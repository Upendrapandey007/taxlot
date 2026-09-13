package com.taxlot.auth.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.auth.domain.SecurityEventType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class SecurityEventRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    fun record(
        credentialId: UUID?,
        eventType: SecurityEventType,
        ipAddress: String?,
        userAgent: String?,
        metadata: Map<String, Any>?
    ) {
        val sql = """
            INSERT INTO security_events (credential_id, event_type, ip_address, user_agent, metadata, created_at)
            VALUES (:credentialId, :eventType, :ipAddress, :userAgent, :metadata::jsonb, :createdAt)
        """.trimIndent()
        val params = MapSqlParameterSource()
            .addValue("credentialId", credentialId)
            .addValue("eventType", eventType.name)
            .addValue("ipAddress", ipAddress)
            .addValue("userAgent", userAgent)
            .addValue("metadata", metadata?.let { objectMapper.writeValueAsString(it) })
            .addValue("createdAt", java.sql.Timestamp.from(Instant.now()))
        
        jdbcTemplate.update(sql, params)
    }
}
