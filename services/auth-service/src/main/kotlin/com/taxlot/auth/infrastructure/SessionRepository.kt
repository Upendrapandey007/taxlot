package com.taxlot.auth.infrastructure

import com.taxlot.auth.domain.Session
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

interface SessionRepository {
    fun save(session: Session): Session
    fun findActiveByCredentialId(credentialId: UUID): List<Session>
    fun revokeById(id: UUID): Int
    fun revokeAllByCredentialId(credentialId: UUID): Int
}

@Repository
class SessionRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SessionRepository {

    private val rowMapper = { rs: ResultSet, _: Int ->
        Session(
            id = rs.getObject("id", java.util.UUID::class.java),
            credentialId = rs.getObject("credential_id", java.util.UUID::class.java),
            userAgent = rs.getString("user_agent"),
            ipAddress = rs.getString("ip_address"),
            lastActiveAt = rs.getTimestamp("last_active_at").toInstant(),
            revokedAt = rs.getTimestamp("revoked_at")?.toInstant(),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    override fun save(session: Session): Session {
        return if (session.id == null) {
            val sql = """
                INSERT INTO sessions (credential_id, user_agent, ip_address, last_active_at, revoked_at, created_at)
                VALUES (:credentialId, :userAgent, :ipAddress, :lastActiveAt, :revokedAt, :createdAt)
                RETURNING id
            """.trimIndent()
            val params = MapSqlParameterSource()
                .addValue("credentialId", session.credentialId)
                .addValue("userAgent", session.userAgent)
                .addValue("ipAddress", session.ipAddress)
                .addValue("lastActiveAt", java.sql.Timestamp.from(session.lastActiveAt))
                .addValue("revokedAt", session.revokedAt?.let { java.sql.Timestamp.from(it) })
                .addValue("createdAt", java.sql.Timestamp.from(session.createdAt))
            
            val id = jdbcTemplate.queryForObject(sql, params, java.util.UUID::class.java)
            session.copy(id = id)
        } else {
            val sql = """
                UPDATE sessions 
                SET last_active_at = :lastActiveAt, revoked_at = :revokedAt
                WHERE id = :id
            """.trimIndent()
            jdbcTemplate.update(sql, mapOf(
                "id" to session.id,
                "lastActiveAt" to java.sql.Timestamp.from(session.lastActiveAt),
                "revokedAt" to session.revokedAt?.let { java.sql.Timestamp.from(it) }
            ))
            session
        }
    }

    override fun findActiveByCredentialId(credentialId: UUID): List<Session> {
        val sql = "SELECT * FROM sessions WHERE credential_id = :credentialId AND revoked_at IS NULL"
        return jdbcTemplate.query(sql, mapOf("credentialId" to credentialId), rowMapper)
    }

    override fun revokeById(id: UUID): Int {
        val sql = "UPDATE sessions SET revoked_at = :revokedAt WHERE id = :id AND revoked_at IS NULL"
        return jdbcTemplate.update(sql, mapOf(
            "id" to id,
            "revokedAt" to java.sql.Timestamp.from(Instant.now())
        ))
    }

    override fun revokeAllByCredentialId(credentialId: UUID): Int {
        val sql = "UPDATE sessions SET revoked_at = :revokedAt WHERE credential_id = :credentialId AND revoked_at IS NULL"
        return jdbcTemplate.update(sql, mapOf(
            "credentialId" to credentialId,
            "revokedAt" to java.sql.Timestamp.from(Instant.now())
        ))
    }
}
