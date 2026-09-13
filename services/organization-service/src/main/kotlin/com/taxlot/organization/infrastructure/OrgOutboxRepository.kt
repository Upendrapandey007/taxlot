package com.taxlot.organization.infrastructure

import com.taxlot.platform.outbox.OutboxRecord
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface OrgOutboxRepository {
    fun save(record: OutboxRecord)
    fun findUnpublished(limit: Int): List<OutboxRecord>
    fun markPublished(id: UUID)
    fun incrementAttempt(id: UUID, error: String)
}

@Repository
class OrgOutboxRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : OrgOutboxRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        OutboxRecord(
            id = UUID.fromString(rs.getString("id")),
            eventId = UUID.fromString(rs.getString("event_id")),
            eventType = rs.getString("event_type"),
            aggregateType = rs.getString("aggregate_type"),
            aggregateId = UUID.fromString(rs.getString("aggregate_id")),
            tenantId = rs.getString("tenant_id")?.let { UUID.fromString(it) },
            routingKey = rs.getString("routing_key"),
            payload = rs.getString("payload"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            publishedAt = rs.getObject("published_at", OffsetDateTime::class.java),
            attemptCount = rs.getInt("attempt_count"),
            lastError = rs.getString("last_error")
        )
    }

    override fun save(record: OutboxRecord) {
        val sql = """
            INSERT INTO org_outbox (id, event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload, created_at, published_at, attempt_count, last_error)
            VALUES (:id, :eventId, :eventType, :aggType, :aggId, :tenantId, :routingKey, :payload, :createdAt, :publishedAt, :attemptCount, :lastError)
        """
        val params = MapSqlParameterSource()
            .addValue("id", record.id)
            .addValue("eventId", record.eventId)
            .addValue("eventType", record.eventType)
            .addValue("aggType", record.aggregateType)
            .addValue("aggId", record.aggregateId)
            .addValue("tenantId", record.tenantId)
            .addValue("routingKey", record.routingKey)
            .addValue("payload", record.payload)
            .addValue("createdAt", record.createdAt)
            .addValue("publishedAt", record.publishedAt)
            .addValue("attemptCount", record.attemptCount)
            .addValue("lastError", record.lastError)
            
        jdbcTemplate.update(sql, params)
    }

    override fun findUnpublished(limit: Int): List<OutboxRecord> {
        val sql = "SELECT * FROM org_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT :limit"
        return jdbcTemplate.query(sql, MapSqlParameterSource("limit", limit), rowMapper)
    }

    override fun markPublished(id: UUID) {
        val sql = "UPDATE org_outbox SET published_at = NOW() WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
    }

    override fun incrementAttempt(id: UUID, error: String) {
        val sql = "UPDATE org_outbox SET attempt_count = attempt_count + 1, last_error = :error WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id).addValue("error", error.take(1000)))
    }
}
