package com.taxlot.auth.infrastructure

import com.taxlot.auth.domain.Credential
import com.taxlot.auth.domain.CredentialStatus
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

interface CredentialRepository {
    fun findById(id: UUID): Credential?
    fun findByEmail(email: String): Credential?
    fun findByUserId(userId: UUID): Credential?
    fun existsByEmail(email: String): Boolean
    fun save(credential: Credential): Credential
    fun updateStatus(userId: UUID, status: CredentialStatus)
    fun updatePasswordHash(userId: UUID, hash: String)
}

@Repository
class CredentialRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : CredentialRepository {

    private val rowMapper = { rs: ResultSet, _: Int ->
        Credential(
            id = rs.getObject("id", java.util.UUID::class.java),
            userId = rs.getObject("user_id", java.util.UUID::class.java),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            status = CredentialStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant()
        )
    }

    override fun findById(id: UUID): Credential? {
        val sql = "SELECT * FROM credentials WHERE id = :id"
        return jdbcTemplate.query(sql, mapOf("id" to id), rowMapper).singleOrNull()
    }

    override fun findByEmail(email: String): Credential? {
        val sql = "SELECT * FROM credentials WHERE email = :email"
        return jdbcTemplate.query(sql, mapOf("email" to email), rowMapper).singleOrNull()
    }

    override fun findByUserId(userId: UUID): Credential? {
        val sql = "SELECT * FROM credentials WHERE user_id = :userId"
        return jdbcTemplate.query(sql, mapOf("userId" to userId), rowMapper).singleOrNull()
    }

    override fun existsByEmail(email: String): Boolean {
        val sql = "SELECT count(*) FROM credentials WHERE email = :email"
        return jdbcTemplate.queryForObject(sql, mapOf("email" to email), Int::class.java)!! > 0
    }

    override fun save(credential: Credential): Credential {
        return if (credential.id == null) {
            val sql = """
                INSERT INTO credentials (user_id, email, password_hash, status, created_at, updated_at)
                VALUES (:userId, :email, :passwordHash, :status, :createdAt, :updatedAt)
                RETURNING id
            """.trimIndent()
            val params = MapSqlParameterSource()
                .addValue("userId", credential.userId)
                .addValue("email", credential.email)
                .addValue("passwordHash", credential.passwordHash)
                .addValue("status", credential.status.name)
                .addValue("createdAt", java.sql.Timestamp.from(credential.createdAt))
                .addValue("updatedAt", java.sql.Timestamp.from(credential.updatedAt))
            
            val id = jdbcTemplate.queryForObject(sql, params, java.util.UUID::class.java)
            credential.copy(id = id)
        } else {
            val sql = """
                UPDATE credentials 
                SET email = :email, password_hash = :passwordHash, status = :status, updated_at = :updatedAt
                WHERE id = :id
            """.trimIndent()
            val params = MapSqlParameterSource()
                .addValue("id", credential.id)
                .addValue("email", credential.email)
                .addValue("passwordHash", credential.passwordHash)
                .addValue("status", credential.status.name)
                .addValue("updatedAt", java.sql.Timestamp.from(Instant.now()))
            
            jdbcTemplate.update(sql, params)
            credential
        }
    }

    override fun updateStatus(userId: UUID, status: CredentialStatus) {
        val sql = "UPDATE credentials SET status = :status, updated_at = :updatedAt WHERE user_id = :userId"
        jdbcTemplate.update(sql, mapOf(
            "status" to status.name,
            "updatedAt" to java.sql.Timestamp.from(Instant.now()),
            "userId" to userId
        ))
    }

    override fun updatePasswordHash(userId: UUID, hash: String) {
        val sql = "UPDATE credentials SET password_hash = :hash, updated_at = :updatedAt WHERE user_id = :userId"
        jdbcTemplate.update(sql, mapOf(
            "hash" to hash,
            "updatedAt" to java.sql.Timestamp.from(Instant.now()),
            "userId" to userId
        ))
    }
}
