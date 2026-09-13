package com.taxlot.modules.auth

import com.taxlot.common.exception.ConflictException
import com.taxlot.common.exception.UnauthorizedException
import com.taxlot.modules.auth.dto.LoginRequest
import com.taxlot.modules.auth.dto.RefreshRequest
import com.taxlot.modules.auth.dto.RegisterRequest
import com.taxlot.modules.auth.dto.TokenResponse
import com.taxlot.modules.auth.token.RefreshToken
import com.taxlot.modules.auth.token.RefreshTokenRepository
import com.taxlot.modules.auth.token.TokenService
import com.taxlot.modules.users.User
import com.taxlot.modules.users.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun register(request: RegisterRequest): TokenResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("User with this email already exists")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName
        )
        val savedUser = userRepository.save(user)
        return issueTokens(savedUser.id, savedUser.email)
    }

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        return issueTokens(user.id, user.email)
    }

    @Transactional
    fun refresh(token: String): TokenResponse {
        val hash = tokenService.hashToken(token)
        val refreshToken = refreshTokenRepository.findByTokenHash(hash)
            ?: throw UnauthorizedException("Invalid or expired refresh token")

        if (refreshToken.expiresAt.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            refreshTokenRepository.revokeByTokenHash(hash)
            throw UnauthorizedException("Refresh token expired")
        }
        if (refreshToken.revokedAt != null) {
            throw UnauthorizedException("Refresh token revoked")
        }

        val user = userRepository.findById(refreshToken.userId)
            ?: throw UnauthorizedException("User not found")

        // Rotate: revoke old, issue new
        refreshTokenRepository.revokeByTokenHash(hash)
        return issueTokens(user.id, user.email)
    }

    @Transactional
    fun logout(token: String) {
        val hash = tokenService.hashToken(token)
        refreshTokenRepository.revokeByTokenHash(hash)
    }

    private fun issueTokens(userId: java.util.UUID, email: String): TokenResponse {
        val accessToken = tokenService.generateAccessToken(userId, email, emptyList())
        val rawRefreshToken = tokenService.generateRefreshToken()
        
        val expiry = OffsetDateTime.now(ZoneOffset.UTC).plusDays(30)
        
        val rt = RefreshToken(
            userId = userId,
            tokenHash = tokenService.hashToken(rawRefreshToken),
            expiresAt = expiry
        )
        refreshTokenRepository.save(rt)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = 15 * 60 // 15 mins
        )
    }
}
