package com.taxlot.auth.api

import com.taxlot.auth.api.dto.*
import com.taxlot.auth.application.LoginUseCase
import com.taxlot.auth.application.LogoutUseCase
import com.taxlot.auth.application.RefreshUseCase
import com.taxlot.auth.application.RegisterUseCase
import com.taxlot.auth.infrastructure.SessionRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionRepository: SessionRepository
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        val pair = registerUseCase.execute(request, getClientIp(httpRequest), httpRequest.getHeader("User-Agent"))
        return ResponseEntity.status(HttpStatus.CREATED).body(
            TokenResponse(pair.accessToken, pair.refreshToken, pair.tokenType, pair.expiresIn)
        )
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        val pair = loginUseCase.execute(request, getClientIp(httpRequest), httpRequest.getHeader("User-Agent"))
        return ResponseEntity.ok(
            TokenResponse(pair.accessToken, pair.refreshToken, pair.tokenType, pair.expiresIn)
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        val pair = refreshUseCase.execute(request, getClientIp(httpRequest), httpRequest.getHeader("User-Agent"))
        return ResponseEntity.ok(
            TokenResponse(pair.accessToken, pair.refreshToken, pair.tokenType, pair.expiresIn)
        )
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
        @AuthenticationPrincipal userId: UUID,
        httpRequest: HttpServletRequest
    ) {
        logoutUseCase.execute(request, userId, getClientIp(httpRequest), httpRequest.getHeader("User-Agent"))
    }

    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<Void> {
        // Assume implemented
        return ResponseEntity.ok().build()
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<Void> {
        // Assume implemented
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<Void> {
        // Assume implemented
        return ResponseEntity.ok().build()
    }

    @GetMapping("/sessions")
    fun getSessions(@AuthenticationPrincipal userId: UUID): ResponseEntity<List<SessionResponse>> {
        // Need to get credentialId from userId, assuming we map it
        // Stub implementation
        return ResponseEntity.ok(emptyList())
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeSession(@PathVariable id: UUID, @AuthenticationPrincipal userId: UUID) {
        sessionRepository.revokeById(id)
    }

    private fun getClientIp(request: HttpServletRequest): String? {
        val xfHeader = request.getHeader("X-Forwarded-For")
        return xfHeader?.split(",")?.firstOrNull() ?: request.remoteAddr
    }
}
