package com.taxlot.user.infrastructure

import com.taxlot.user.domain.UserProfile
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface UserProfileRepository {
    fun findByUserId(userId: UUID): UserProfile?
    fun save(profile: UserProfile): UserProfile
    fun update(profile: UserProfile): UserProfile
    fun existsByUserId(userId: UUID): Boolean
}

@Repository
class UserProfileRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserProfileRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        UserProfile(
            id = UUID.fromString(rs.getString("id")),
            userId = UUID.fromString(rs.getString("user_id")),
            email = rs.getString("email"),
            fullName = rs.getString("full_name"),
            avatarUrl = rs.getString("avatar_url"),
            locale = rs.getString("locale"),
            timezone = rs.getString("timezone"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    override fun findByUserId(userId: UUID): UserProfile? {
        val sql = "SELECT * FROM user_profiles WHERE user_id = :userId"
        val params = MapSqlParameterSource().addValue("userId", userId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun save(profile: UserProfile): UserProfile {
        val sql = """
            INSERT INTO user_profiles (id, user_id, email, full_name, avatar_url, locale, timezone, is_active, created_at, updated_at)
            VALUES (:id, :userId, :email, :fullName, :avatarUrl, :locale, :timezone, :isActive, :createdAt, :updatedAt)
        """
        val params = MapSqlParameterSource()
            .addValue("id", profile.id)
            .addValue("userId", profile.userId)
            .addValue("email", profile.email)
            .addValue("fullName", profile.fullName)
            .addValue("avatarUrl", profile.avatarUrl)
            .addValue("locale", profile.locale)
            .addValue("timezone", profile.timezone)
            .addValue("isActive", profile.isActive)
            .addValue("createdAt", profile.createdAt)
            .addValue("updatedAt", profile.updatedAt)
        
        jdbcTemplate.update(sql, params)
        return profile
    }

    override fun update(profile: UserProfile): UserProfile {
        val sql = """
            UPDATE user_profiles
            SET email = :email, full_name = :fullName, avatar_url = :avatarUrl, locale = :locale, 
                timezone = :timezone, is_active = :isActive, updated_at = :updatedAt
            WHERE user_id = :userId
        """
        val params = MapSqlParameterSource()
            .addValue("userId", profile.userId)
            .addValue("email", profile.email)
            .addValue("fullName", profile.fullName)
            .addValue("avatarUrl", profile.avatarUrl)
            .addValue("locale", profile.locale)
            .addValue("timezone", profile.timezone)
            .addValue("isActive", profile.isActive)
            .addValue("updatedAt", OffsetDateTime.now())
        
        jdbcTemplate.update(sql, params)
        return findByUserId(profile.userId) ?: profile
    }

    override fun existsByUserId(userId: UUID): Boolean {
        val sql = "SELECT count(1) FROM user_profiles WHERE user_id = :userId"
        val params = MapSqlParameterSource().addValue("userId", userId)
        return (jdbcTemplate.queryForObject(sql, params, Int::class.java) ?: 0) > 0
    }
}
