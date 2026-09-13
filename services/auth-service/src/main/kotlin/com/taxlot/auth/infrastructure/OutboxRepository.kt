package com.taxlot.auth.infrastructure

import com.taxlot.platform.outbox.OutboxRecord
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

interface OutboxRepository {
    fun save(record: OutboxRecord): OutboxRecord
    fun findUnpublished(limit: Int): List<OutboxRecord>
    fun markPublished(id: UUID)
    fun incrementAttempt(id: UUID, error: String)
}

@Repository
class OutboxRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : OutboxRepository {

    private val rowMapper = { rs: ResultSet, _: Int ->
        OutboxRecord(
            id = rs.getObject("id", java.util.UUID::class.java),
            eventId = rs.getObject("event_id", java.util.UUID::class.java),
            eventType = rs.getString("event_type"),
            aggregateType = rs.getString("aggregate_type"),
            aggregateId = rs.getObject("aggregate_id", java.util.UUID::class.java),
            tenantId = rs.getObject("tenant_id", java.util.UUID::class.java),
            routingKey = rs.getString("routing_key"),
            payload = rs.getString("payload"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            publishedAt = rs.getTimestamp("published_at")?.toInstant(),
            attemptCount = rs.getInt("attempt_count"),
            lastError = rs.getString("last_error")
        )
    }

    override fun save(record: OutboxRecord): OutboxRecord {
        val sql = """
            INSERT INTO auth_outbox (event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload, created_at)
            VALUES (:eventId, :eventType, :aggregateType, :aggregateId, :tenantId, :routingKey, :payload, :createdAt)
            RETURNING id
        """.trimIndent()
        val params = MapSqlParameterSource()
            .addValue("eventId", record.eventId)
            .addValue("eventType", record.eventType)
            .addValue("aggregateType", record.aggregateType)
            .addValue("aggregateId", record.aggregateId)
            .addValue("tenantId", record.tenantId)
            .addValue("routingKey", record.routingKey)
            .addValue("payload", record.payload)
            .addValue("createdAt", java.sql.Timestamp.from(record.createdAt))
        
        val id = jdbcTemplate.queryForObject(sql, params, java.util.UUID::class.java)
        return record.copy(id = id)
    }

    override fun findUnpublished(limit: Int): List<OutboxRecord> {
        val sql = """
            SELECT * FROM auth_outbox 
            WHERE published_at IS NULL AND attempt_count < 5
            ORDER BY created_at ASC 
            LIMIT :limit
        """.trimIndent()
        return jdbcTemplate.query(sql, mapOf("limit" to limit), rowMapper)
    }

    override fun markPublished(id: UUID) {
        val sql = "UPDATE auth_outbox SET published_at = :publishedAt WHERE id = :id"
        jdbcTemplate.update(sql, mapOf(
            "id" to id,
            "publishedAt" to java.sql.Timestamp.from(Instant.now())
        ))
    }

    override fun incrementAttempt(id: UUID, error: String) {
        val sql = "UPDATE auth_outbox SET attempt_count = attempt_count + 1, last_error = :error WHERE id = :id"
        jdbcTemplate.update(sql, mapOf(
            "id" to id,
            "error" to error
        ))
    }
}
