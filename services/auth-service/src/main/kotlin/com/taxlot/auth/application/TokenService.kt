package com.taxlot.auth.application

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class TokenService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    private val secureRandom = SecureRandom()

    fun generateAccessToken(userId: UUID, email: String): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(jwtProperties.accessExpiryMinutes * 60)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact()
    }

    fun validateAccessToken(token: String): JwtClaims? {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            JwtClaims(
                userId = UUID.fromString(claims.subject),
                email = claims.get("email", String::class.java)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun generateRefreshToken(): String {
        val randomBytes = ByteArray(64)
        secureRandom.nextBytes(randomBytes)
        return randomBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
