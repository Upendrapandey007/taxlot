package com.taxlot.auth.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, max = 100) val password: String,
    @field:NotBlank @field:Size(min = 2, max = 100) val fullName: String
)

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class LogoutRequest(
    @field:NotBlank val refreshToken: String
)

data class VerifyEmailRequest(
    @field:NotBlank val token: String
)

data class ForgotPasswordRequest(
    @field:NotBlank @field:Email val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 8, max = 100) val newPassword: String
)

data class SessionResponse(
    val id: java.util.UUID,
    val userAgent: String?,
    val ipAddress: String?,
    val lastActiveAt: java.time.Instant,
    val createdAt: java.time.Instant
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
