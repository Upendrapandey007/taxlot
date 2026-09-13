package com.taxlot.auth.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.auth.api.dto.RegisterRequest
import com.taxlot.auth.domain.Credential
import com.taxlot.auth.domain.RefreshToken
import com.taxlot.auth.domain.Session
import com.taxlot.auth.infrastructure.CredentialRepository
import com.taxlot.auth.infrastructure.OutboxRepository
import com.taxlot.auth.infrastructure.RefreshTokenRepository
import com.taxlot.auth.infrastructure.SessionRepository
import com.taxlot.platform.event.EventEnvelope
import com.taxlot.platform.exception.ConflictException
import com.taxlot.platform.outbox.OutboxRecord
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RegisterUseCase(
    private val credentialRepository: CredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val sessionRepository: SessionRepository,
    private val outboxRepository: OutboxRepository,
    private val tokenService: TokenService,
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
    private val jwtProperties: JwtProperties
) {
    @Transactional
    fun execute(request: RegisterRequest, ipAddress: String?, userAgent: String?): TokenPair {
        if (credentialRepository.existsByEmail(request.email)) {
            throw ConflictException("Email is already registered")
        }

        val userId = UUID.randomUUID()
        val credential = Credential(
            userId = userId,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)
        )
        val savedCredential = credentialRepository.save(credential)

        // Publish UserRegistered Event via Outbox
        val eventPayload = mapOf(
            "userId" to userId,
            "email" to request.email,
            "fullName" to request.fullName
        )
        val envelope = EventEnvelope(
            eventId = UUID.randomUUID(),
            eventType = "UserRegistered",
            timestamp = Instant.now(),
            payload = eventPayload
        )
        
        outboxRepository.save(
            OutboxRecord(
                eventId = envelope.eventId,
                eventType = envelope.eventType,
                aggregateType = "User",
                aggregateId = userId,
                routingKey = "auth.user.registered",
                payload = objectMapper.writeValueAsString(envelope)
            )
        )

        // Generate Tokens
        val accessToken = tokenService.generateAccessToken(userId, request.email)
        val rawRefreshToken = tokenService.generateRefreshToken()
        
        refreshTokenRepository.save(
            RefreshToken(
                credentialId = savedCredential.id!!,
                tokenHash = tokenService.hashToken(rawRefreshToken),
                deviceInfo = userAgent,
                ipAddress = ipAddress,
                expiresAt = Instant.now().plusSeconds(jwtProperties.refreshExpiryDays * 24 * 60 * 60)
            )
        )
        
        sessionRepository.save(
            Session(
                credentialId = savedCredential.id,
                userAgent = userAgent,
                ipAddress = ipAddress
            )
        )

        return TokenPair(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = jwtProperties.accessExpiryMinutes * 60
        )
    }
}
