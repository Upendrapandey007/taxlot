package com.taxlot.modules.auth

import com.taxlot.modules.auth.dto.LoginRequest
import com.taxlot.modules.auth.dto.RefreshRequest
import com.taxlot.modules.auth.dto.RegisterRequest
import com.taxlot.modules.auth.dto.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): TokenResponse {
        return authService.register(request)
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive tokens")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse {
        return authService.login(request)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    fun refresh(@Valid @RequestBody request: RefreshRequest): TokenResponse {
        return authService.refresh(request.refreshToken)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    fun logout(@Valid @RequestBody request: RefreshRequest) {
        authService.logout(request.refreshToken)
    }
}
