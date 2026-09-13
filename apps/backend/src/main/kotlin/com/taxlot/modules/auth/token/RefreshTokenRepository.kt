package com.taxlot.modules.auth.token

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class RefreshTokenRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {
    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        RefreshToken(
            id = UUID.fromString(rs.getString("id")),
            userId = UUID.fromString(rs.getString("user_id")),
            tokenHash = rs.getString("token_hash"),
            deviceInfo = rs.getString("device_info"),
            expiresAt = rs.getObject("expires_at", OffsetDateTime::class.java),
            revokedAt = rs.getObject("revoked_at", OffsetDateTime::class.java),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
        )
    }

    fun save(token: RefreshToken): RefreshToken {
        val sql = """
            INSERT INTO refresh_tokens (id, user_id, token_hash, device_info, expires_at, revoked_at, created_at)
            VALUES (:id, :userId, :tokenHash, :deviceInfo, :expiresAt, :revokedAt, :createdAt)
            ON CONFLICT (token_hash) DO UPDATE SET
                revoked_at = :revokedAt
            RETURNING *
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", token.id)
            .addValue("userId", token.userId)
            .addValue("tokenHash", token.tokenHash)
            .addValue("deviceInfo", token.deviceInfo)
            .addValue("expiresAt", token.expiresAt)
            .addValue("revokedAt", token.revokedAt)
            .addValue("createdAt", token.createdAt)

        return jdbcTemplate.queryForObject(sql, params, rowMapper)!!
    }

    fun findByTokenHash(hash: String): RefreshToken? {
        val sql = "SELECT * FROM refresh_tokens WHERE token_hash = :hash"
        val params = MapSqlParameterSource("hash", hash)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun revokeByTokenHash(hash: String) {
        val sql = "UPDATE refresh_tokens SET revoked_at = :now WHERE token_hash = :hash"
        val params = MapSqlParameterSource()
            .addValue("hash", hash)
            .addValue("now", OffsetDateTime.now(ZoneOffset.UTC))
        jdbcTemplate.update(sql, params)
    }

    fun revokeAllByUserId(userId: UUID) {
        val sql = "UPDATE refresh_tokens SET revoked_at = :now WHERE user_id = :userId AND revoked_at IS NULL"
        val params = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("now", OffsetDateTime.now(ZoneOffset.UTC))
        jdbcTemplate.update(sql, params)
    }

    fun deleteExpired() {
        val sql = "DELETE FROM refresh_tokens WHERE expires_at < :now"
        val params = MapSqlParameterSource("now", OffsetDateTime.now(ZoneOffset.UTC))
        jdbcTemplate.update(sql, params)
    }
}
