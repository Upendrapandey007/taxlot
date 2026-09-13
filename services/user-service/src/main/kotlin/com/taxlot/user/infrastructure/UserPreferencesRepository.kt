package com.taxlot.user.infrastructure

import com.taxlot.user.domain.UserPreferences
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface UserPreferencesRepository {
    fun findByUserId(userId: UUID): UserPreferences?
    fun save(prefs: UserPreferences): UserPreferences
    fun update(prefs: UserPreferences): UserPreferences
}

@Repository
class UserPreferencesRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : UserPreferencesRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        UserPreferences(
            id = UUID.fromString(rs.getString("id")),
            userId = UUID.fromString(rs.getString("user_id")),
            emailNotifications = rs.getBoolean("email_notifications"),
            pushNotifications = rs.getBoolean("push_notifications"),
            currencyDisplay = rs.getString("currency_display"),
            dateFormat = rs.getString("date_format"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    override fun findByUserId(userId: UUID): UserPreferences? {
        val sql = "SELECT * FROM user_preferences WHERE user_id = :userId"
        val params = MapSqlParameterSource().addValue("userId", userId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun save(prefs: UserPreferences): UserPreferences {
        val sql = """
            INSERT INTO user_preferences (id, user_id, email_notifications, push_notifications, currency_display, date_format, created_at, updated_at)
            VALUES (:id, :userId, :emailNotifications, :pushNotifications, :currencyDisplay, :dateFormat, :createdAt, :updatedAt)
        """
        val params = MapSqlParameterSource()
            .addValue("id", prefs.id)
            .addValue("userId", prefs.userId)
            .addValue("emailNotifications", prefs.emailNotifications)
            .addValue("pushNotifications", prefs.pushNotifications)
            .addValue("currencyDisplay", prefs.currencyDisplay)
            .addValue("dateFormat", prefs.dateFormat)
            .addValue("createdAt", prefs.createdAt)
            .addValue("updatedAt", prefs.updatedAt)
        
        jdbcTemplate.update(sql, params)
        return prefs
    }

    override fun update(prefs: UserPreferences): UserPreferences {
        val sql = """
            UPDATE user_preferences
            SET email_notifications = :emailNotifications, push_notifications = :pushNotifications, 
                currency_display = :currencyDisplay, date_format = :dateFormat, updated_at = :updatedAt
            WHERE user_id = :userId
        """
        val params = MapSqlParameterSource()
            .addValue("userId", prefs.userId)
            .addValue("emailNotifications", prefs.emailNotifications)
            .addValue("pushNotifications", prefs.pushNotifications)
            .addValue("currencyDisplay", prefs.currencyDisplay)
            .addValue("dateFormat", prefs.dateFormat)
            .addValue("updatedAt", OffsetDateTime.now())
        
        jdbcTemplate.update(sql, params)
        return findByUserId(prefs.userId) ?: prefs
    }
}
