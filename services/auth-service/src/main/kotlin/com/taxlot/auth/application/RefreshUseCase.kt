package com.taxlot.auth.application

import com.taxlot.auth.api.dto.RefreshRequest
import com.taxlot.auth.domain.RefreshToken
import com.taxlot.auth.infrastructure.CredentialRepository
import com.taxlot.auth.infrastructure.RefreshTokenRepository
import com.taxlot.platform.exception.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RefreshUseCase(
    private val credentialRepository: CredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties
) {
    @Transactional
    fun execute(request: RefreshRequest, ipAddress: String?, userAgent: String?): TokenPair {
        val hash = tokenService.hashToken(request.refreshToken)
        val existingToken = refreshTokenRepository.findByTokenHash(hash)
        
        if (existingToken == null || existingToken.revokedAt != null || existingToken.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        // Revoke old token
        refreshTokenRepository.revokeByTokenHash(hash)

        // Find credential to generate new tokens
        val credential = credentialRepository.findById(existingToken.credentialId) 
            ?: throw UnauthorizedException("Invalid credential")

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

        return TokenPair(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = jwtProperties.accessExpiryMinutes * 60
        )
    }
}
