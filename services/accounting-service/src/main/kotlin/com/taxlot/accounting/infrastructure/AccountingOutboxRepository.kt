package com.taxlot.accounting.infrastructure

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

data class OutboxRecord(
    val id: UUID = UUID.randomUUID(),
    val eventId: UUID = UUID.randomUUID(),
    val eventType: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val tenantId: UUID?,
    val routingKey: String,
    val payload: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val publishedAt: OffsetDateTime? = null,
    val attemptCount: Int = 0,
    val lastError: String? = null
)

@Repository
class AccountingOutboxRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        OutboxRecord(
            id = rs.getObject("id", UUID::class.java),
            eventId = rs.getObject("event_id", UUID::class.java),
            eventType = rs.getString("event_type"),
            aggregateType = rs.getString("aggregate_type"),
            aggregateId = rs.getObject("aggregate_id", UUID::class.java),
            tenantId = rs.getObject("tenant_id", UUID::class.java),
            routingKey = rs.getString("routing_key"),
            payload = rs.getString("payload"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            publishedAt = rs.getObject("published_at", OffsetDateTime::class.java),
            attemptCount = rs.getInt("attempt_count"),
            lastError = rs.getString("last_error")
        )
    }

    fun save(record: OutboxRecord): OutboxRecord {
        val sql = """
            INSERT INTO accounting_outbox (
                id, event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload, created_at, published_at, attempt_count, last_error
            ) VALUES (
                :id, :eventId, :eventType, :aggregateType, :aggregateId, :tenantId, :routingKey, :payload, :createdAt, :publishedAt, :attemptCount, :lastError
            )
        """
        val params = MapSqlParameterSource()
            .addValue("id", record.id)
            .addValue("eventId", record.eventId)
            .addValue("eventType", record.eventType)
            .addValue("aggregateType", record.aggregateType)
            .addValue("aggregateId", record.aggregateId)
            .addValue("tenantId", record.tenantId)
            .addValue("routingKey", record.routingKey)
            .addValue("payload", record.payload)
            .addValue("createdAt", record.createdAt)
            .addValue("publishedAt", record.publishedAt)
            .addValue("attemptCount", record.attemptCount)
            .addValue("lastError", record.lastError)
        jdbcTemplate.update(sql, params)
        return record
    }

    fun findUnpublished(limit: Int): List<OutboxRecord> {
        val sql = "SELECT * FROM accounting_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT :limit"
        return jdbcTemplate.query(sql, MapSqlParameterSource("limit", limit), rowMapper)
    }

    fun markPublished(id: UUID) {
        val sql = "UPDATE accounting_outbox SET published_at = NOW() WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
    }

    fun incrementAttempt(id: UUID, error: String) {
        val sql = "UPDATE accounting_outbox SET attempt_count = attempt_count + 1, last_error = :error WHERE id = :id"
        val params = MapSqlParameterSource()
            .addValue("id", id)
            .addValue("error", error.take(1000))
        jdbcTemplate.update(sql, params)
    }
}
