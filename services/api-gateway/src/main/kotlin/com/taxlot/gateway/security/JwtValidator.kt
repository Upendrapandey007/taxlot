package com.taxlot.gateway.security

import com.taxlot.gateway.config.GatewayProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtValidator(properties: GatewayProperties) {
    private val key: SecretKey = Keys.hmacShaKeyFor(properties.jwtSecret.toByteArray(StandardCharsets.UTF_8))

    fun validateAndParse(token: String): JwtClaims {
        try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            
            return JwtClaims(
                userId = UUID.fromString(claims.subject),
                email = claims.get("email", String::class.java)
            )
        } catch (ex: Exception) {
            throw InvalidJwtException("Invalid token")
        }
    }
}

data class JwtClaims(val userId: UUID, val email: String?)
class InvalidJwtException(message: String) : RuntimeException(message)
