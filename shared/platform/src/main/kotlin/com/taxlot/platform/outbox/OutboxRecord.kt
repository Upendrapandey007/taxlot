package com.taxlot.platform.outbox

import java.time.OffsetDateTime
import java.util.UUID

data class OutboxRecord(
    val id: UUID = UUID.randomUUID(),
    val eventId: UUID,
    val eventType: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val tenantId: UUID?,
    val routingKey: String,
    val payload: String, // JSON-serialized EventEnvelope
    val createdAt: OffsetDateTime,
    val publishedAt: OffsetDateTime? = null,
    val attemptCount: Int = 0,
    val lastError: String? = null
)
