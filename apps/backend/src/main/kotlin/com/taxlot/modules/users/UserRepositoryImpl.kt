package com.taxlot.modules.users

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        User(
            id = UUID.fromString(rs.getString("id")),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            fullName = rs.getString("full_name"),
            emailVerifiedAt = rs.getObject("email_verified_at", OffsetDateTime::class.java),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    override fun findByEmail(email: String): User? {
        val sql = "SELECT * FROM users WHERE email = :email"
        val params = MapSqlParameterSource("email", email)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun findById(id: UUID): User? {
        val sql = "SELECT * FROM users WHERE id = :id"
        val params = MapSqlParameterSource("id", id)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun save(user: User): User {
        val sql = """
            INSERT INTO users (id, email, password_hash, full_name, email_verified_at, is_active, created_at, updated_at)
            VALUES (:id, :email, :passwordHash, :fullName, :emailVerifiedAt, :isActive, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET 
                email = :email,
                password_hash = :passwordHash,
                full_name = :fullName,
                is_active = :isActive,
                updated_at = :updatedAt
            RETURNING *
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", user.id)
            .addValue("email", user.email)
            .addValue("passwordHash", user.passwordHash)
            .addValue("fullName", user.fullName)
            .addValue("emailVerifiedAt", user.emailVerifiedAt)
            .addValue("isActive", user.isActive)
            .addValue("createdAt", user.createdAt)
            .addValue("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))

        return jdbcTemplate.queryForObject(sql, params, rowMapper)!!
    }

    override fun existsByEmail(email: String): Boolean {
        val sql = "SELECT count(1) FROM users WHERE email = :email"
        val params = MapSqlParameterSource("email", email)
        val count = jdbcTemplate.queryForObject(sql, params, Int::class.java)
        return (count ?: 0) > 0
    }
}
