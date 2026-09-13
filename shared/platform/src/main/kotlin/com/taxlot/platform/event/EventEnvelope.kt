package com.taxlot.platform.event

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

data class EventEnvelope(
    val eventId: UUID = UUID.randomUUID(),
    val eventType: String,
    val eventVersion: Int = 1,
    val occurredAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val producer: String,
    val tenantId: UUID? = null,
    val aggregateType: String,
    val aggregateId: UUID,
    val traceId: String? = null,
    val payload: Any
)
