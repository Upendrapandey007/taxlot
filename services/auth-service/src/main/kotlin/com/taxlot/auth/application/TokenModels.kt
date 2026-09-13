package com.taxlot.auth.application

import java.util.UUID

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class JwtClaims(
    val userId: UUID,
    val email: String
)
