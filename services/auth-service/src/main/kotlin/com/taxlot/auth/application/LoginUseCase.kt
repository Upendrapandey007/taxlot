package com.taxlot.auth.application

import com.taxlot.auth.api.dto.LoginRequest
import com.taxlot.auth.domain.CredentialStatus
import com.taxlot.auth.domain.RefreshToken
import com.taxlot.auth.domain.SecurityEventType
import com.taxlot.auth.domain.Session
import com.taxlot.auth.infrastructure.CredentialRepository
import com.taxlot.auth.infrastructure.RefreshTokenRepository
import com.taxlot.auth.infrastructure.SecurityEventRepository
import com.taxlot.auth.infrastructure.SessionRepository
import com.taxlot.platform.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LoginUseCase(
    private val credentialRepository: CredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val sessionRepository: SessionRepository,
    private val securityEventRepository: SecurityEventRepository,
    private val tokenService: TokenService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProperties: JwtProperties
) {
    @Transactional
    fun execute(request: LoginRequest, ipAddress: String?, userAgent: String?): TokenPair {
        val credential = credentialRepository.findByEmail(request.email)
        
        if (credential == null || credential.status != CredentialStatus.ACTIVE) {
            securityEventRepository.record(
                credentialId = credential?.id,
                eventType = SecurityEventType.LOGIN_FAILED,
                ipAddress = ipAddress,
                userAgent = userAgent,
                metadata = mapOf("reason" to "invalid_credentials_or_status")
            )
            throw UnauthorizedException("Invalid email or password")
        }

        if (!passwordEncoder.matches(request.password, credential.passwordHash)) {
            securityEventRepository.record(
                credentialId = credential.id,
                eventType = SecurityEventType.LOGIN_FAILED,
                ipAddress = ipAddress,
                userAgent = userAgent,
                metadata = mapOf("reason" to "wrong_password")
            )
            throw UnauthorizedException("Invalid email or password")
        }

        val accessToken = tokenService.generateAccessToken(credential.userId, credential.email)
        val rawRefreshToken = tokenService.generateRefreshToken()

        refreshTokenRepository.save(
            RefreshToken(
                credentialId = credential.id!!,
                tokenHash = tokenService.hashToken(rawRefreshToken),
                deviceInfo = userAgent,
                ipAddress = ipAddress,
                expiresAt = Instant.now().plusSeconds(jwtProperties.refreshExpiryDays * 24 * 60 * 60)
            )
        )

        sessionRepository.save(
            Session(
                credentialId = credential.id,
                userAgent = userAgent,
                ipAddress = ipAddress
            )
        )

        securityEventRepository.record(
            credentialId = credential.id,
            eventType = SecurityEventType.LOGIN_SUCCESS,
            ipAddress = ipAddress,
            userAgent = userAgent,
            metadata = null
        )

        return TokenPair(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = jwtProperties.accessExpiryMinutes * 60
        )
    }
}
