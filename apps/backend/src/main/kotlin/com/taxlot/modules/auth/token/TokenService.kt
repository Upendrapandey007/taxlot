package com.taxlot.modules.auth.token

import com.taxlot.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class TokenService(
    private val jwtProperties: JwtProperties
) {
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateAccessToken(userId: UUID, email: String, organizationIds: List<UUID>): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(jwtProperties.accessExpiryMinutes * 60)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("orgs", organizationIds.map { it.toString() })
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()
    }

    fun validateAccessToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    fun generateRefreshToken(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashToken(token: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
