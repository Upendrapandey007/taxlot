package com.taxlot.payment.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.payment.domain.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class PaymentRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun generatePaymentNumber(year: Int): String {
        val countSql = "SELECT COUNT(*) FROM payments WHERE extract(year from created_at) = :year"
        val count = jdbcTemplate.queryForObject(countSql, MapSqlParameterSource("year", year), Int::class.java) ?: 0
        return "PAY-$year-${String.format("%04d", count + 1)}"
    }

    fun save(payment: Payment): Payment {
        val sql = """
            INSERT INTO payments (
                id, organization_id, payment_number, customer_id, amount, currency_code,
                payment_date, payment_method, reference, notes, status, created_at, updated_at
            ) VALUES (
                :id, :organizationId, :paymentNumber, :customerId, :amount, :currencyCode,
                :paymentDate, :paymentMethod::payment_method_type, :reference, :notes, :status::payment_status, :createdAt, :updatedAt
            )
        """.trimIndent()
        val params = MapSqlParameterSource()
            .addValue("id", payment.id)
            .addValue("organizationId", payment.organizationId)
            .addValue("paymentNumber", payment.paymentNumber)
            .addValue("customerId", payment.customerId)
            .addValue("amount", payment.amount)
            .addValue("currencyCode", payment.currencyCode)
            .addValue("paymentDate", payment.paymentDate)
            .addValue("paymentMethod", payment.paymentMethod.name)
            .addValue("reference", payment.reference)
            .addValue("notes", payment.notes)
            .addValue("status", payment.status.name)
            .addValue("createdAt", payment.createdAt)
            .addValue("updatedAt", payment.updatedAt)
            
        jdbcTemplate.update(sql, params)
        return payment
    }

    fun updateStatus(id: UUID, organizationId: UUID, status: PaymentStatus) {
        val sql = "UPDATE payments SET status = :status::payment_status, updated_at = :updatedAt WHERE id = :id AND organization_id = :organizationId"
        jdbcTemplate.update(sql, MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId).addValue("status", status.name).addValue("updatedAt", OffsetDateTime.now()))
    }

    fun findById(id: UUID, organizationId: UUID): Payment? {
        val sql = "SELECT * FROM payments WHERE id = :id AND organization_id = :organizationId"
        return jdbcTemplate.query(sql, MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId)) { rs, _ -> mapRowToPayment(rs) }.firstOrNull()
    }
    
    fun findAll(organizationId: UUID, page: Int, size: Int): List<Payment> {
        val sql = "SELECT * FROM payments WHERE organization_id = :organizationId ORDER BY created_at DESC LIMIT :limit OFFSET :offset"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId).addValue("limit", size).addValue("offset", page * size)
        return jdbcTemplate.query(sql, params) { rs, _ -> mapRowToPayment(rs) }
    }

    private fun mapRowToPayment(rs: ResultSet): Payment {
        return Payment(
            id = rs.getObject("id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            paymentNumber = rs.getString("payment_number"),
            customerId = rs.getObject("customer_id", UUID::class.java),
            amount = rs.getBigDecimal("amount"),
            currencyCode = rs.getString("currency_code"),
            paymentDate = rs.getDate("payment_date").toLocalDate(),
            paymentMethod = PaymentMethodType.valueOf(rs.getString("payment_method")),
            reference = rs.getString("reference"),
            notes = rs.getString("notes"),
            status = PaymentStatus.valueOf(rs.getString("status")),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }
}

@Repository
class PaymentAllocationRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun saveAll(allocations: List<PaymentAllocation>) {
        val sql = """
            INSERT INTO payment_allocations (id, payment_id, organization_id, invoice_id, allocated_amount, created_at)
            VALUES (:id, :paymentId, :organizationId, :invoiceId, :allocatedAmount, :createdAt)
        """.trimIndent()
        val batchParams = allocations.map { alloc ->
            MapSqlParameterSource()
                .addValue("id", alloc.id)
                .addValue("paymentId", alloc.paymentId)
                .addValue("organizationId", alloc.organizationId)
                .addValue("invoiceId", alloc.invoiceId)
                .addValue("allocatedAmount", alloc.allocatedAmount)
                .addValue("createdAt", alloc.createdAt)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }
}

@Repository
class PaymentOutboxRepository(private val jdbcTemplate: NamedParameterJdbcTemplate, private val objectMapper: ObjectMapper) {
    fun save(eventId: UUID, eventType: String, aggregateType: String, aggregateId: UUID, tenantId: UUID, routingKey: String, payload: Any) {
        val sql = """
            INSERT INTO payment_outbox (event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload)
            VALUES (:eventId, :eventType, :aggregateType, :aggregateId, :tenantId, :routingKey, :payload)
        """.trimIndent()
        
        val params = MapSqlParameterSource()
            .addValue("eventId", eventId)
            .addValue("eventType", eventType)
            .addValue("aggregateType", aggregateType)
            .addValue("aggregateId", aggregateId)
            .addValue("tenantId", tenantId)
            .addValue("routingKey", routingKey)
            .addValue("payload", objectMapper.writeValueAsString(payload))
            
        jdbcTemplate.update(sql, params)
    }

    fun findUnpublished(): List<Map<String, Any>> {
        val sql = "SELECT * FROM payment_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT 50"
        return jdbcTemplate.queryForList(sql, MapSqlParameterSource())
    }

    fun markPublished(id: UUID) {
        val sql = "UPDATE payment_outbox SET published_at = NOW() WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
    }
}
