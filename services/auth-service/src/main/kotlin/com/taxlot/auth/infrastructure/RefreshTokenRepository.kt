package com.taxlot.auth.infrastructure

import com.taxlot.auth.domain.RefreshToken
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findByTokenHash(hash: String): RefreshToken?
    fun revokeByTokenHash(hash: String): Int
    fun revokeAllByCredentialId(credentialId: UUID): Int
    fun findActiveByCredentialId(credentialId: UUID): List<RefreshToken>
    fun deleteExpired(): Int
}

@Repository
class RefreshTokenRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : RefreshTokenRepository {

    private val rowMapper = { rs: ResultSet, _: Int ->
        RefreshToken(
            id = rs.getObject("id", java.util.UUID::class.java),
            credentialId = rs.getObject("credential_id", java.util.UUID::class.java),
            tokenHash = rs.getString("token_hash"),
            deviceInfo = rs.getString("device_info"),
            ipAddress = rs.getString("ip_address"),
            expiresAt = rs.getTimestamp("expires_at").toInstant(),
            revokedAt = rs.getTimestamp("revoked_at")?.toInstant(),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    override fun save(token: RefreshToken): RefreshToken {
        return if (token.id == null) {
            val sql = """
                INSERT INTO refresh_tokens (credential_id, token_hash, device_info, ip_address, expires_at, revoked_at, created_at)
                VALUES (:credentialId, :tokenHash, :deviceInfo, :ipAddress, :expiresAt, :revokedAt, :createdAt)
                RETURNING id
            """.trimIndent()
            val params = MapSqlParameterSource()
                .addValue("credentialId", token.credentialId)
                .addValue("tokenHash", token.tokenHash)
                .addValue("deviceInfo", token.deviceInfo)
                .addValue("ipAddress", token.ipAddress)
                .addValue("expiresAt", java.sql.Timestamp.from(token.expiresAt))
                .addValue("revokedAt", token.revokedAt?.let { java.sql.Timestamp.from(it) })
                .addValue("createdAt", java.sql.Timestamp.from(token.createdAt))
            
            val id = jdbcTemplate.queryForObject(sql, params, java.util.UUID::class.java)
            token.copy(id = id)
        } else {
            val sql = """
                UPDATE refresh_tokens 
                SET revoked_at = :revokedAt
                WHERE id = :id
            """.trimIndent()
            jdbcTemplate.update(sql, mapOf(
                "id" to token.id,
                "revokedAt" to token.revokedAt?.let { java.sql.Timestamp.from(it) }
            ))
            token
        }
    }

    override fun findByTokenHash(hash: String): RefreshToken? {
        val sql = "SELECT * FROM refresh_tokens WHERE token_hash = :hash"
        return jdbcTemplate.query(sql, mapOf("hash" to hash), rowMapper).singleOrNull()
    }

    override fun revokeByTokenHash(hash: String): Int {
        val sql = "UPDATE refresh_tokens SET revoked_at = :revokedAt WHERE token_hash = :hash AND revoked_at IS NULL"
        return jdbcTemplate.update(sql, mapOf(
            "hash" to hash,
            "revokedAt" to java.sql.Timestamp.from(Instant.now())
        ))
    }

    override fun revokeAllByCredentialId(credentialId: UUID): Int {
        val sql = "UPDATE refresh_tokens SET revoked_at = :revokedAt WHERE credential_id = :credentialId AND revoked_at IS NULL"
        return jdbcTemplate.update(sql, mapOf(
            "credentialId" to credentialId,
            "revokedAt" to java.sql.Timestamp.from(Instant.now())
        ))
    }

    override fun findActiveByCredentialId(credentialId: UUID): List<RefreshToken> {
        val sql = "SELECT * FROM refresh_tokens WHERE credential_id = :credentialId AND revoked_at IS NULL AND expires_at > NOW()"
        return jdbcTemplate.query(sql, mapOf("credentialId" to credentialId), rowMapper)
    }

    override fun deleteExpired(): Int {
        val sql = "DELETE FROM refresh_tokens WHERE expires_at < NOW() OR revoked_at < NOW() - INTERVAL '30 days'"
        return jdbcTemplate.update(sql, emptyMap<String, Any>())
    }
}
