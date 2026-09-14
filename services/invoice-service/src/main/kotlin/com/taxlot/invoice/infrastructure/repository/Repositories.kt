package com.taxlot.invoice.infrastructure.repository

import com.taxlot.invoice.domain.*
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class InvoiceRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Invoice(
            id = UUID.fromString(rs.getString("id")),
            organizationId = UUID.fromString(rs.getString("organization_id")),
            customerId = UUID.fromString(rs.getString("customer_id")),
            invoiceNumber = rs.getString("invoice_number"),
            status = InvoiceStatus.valueOf(rs.getString("status")),
            issueDate = rs.getObject("issue_date", LocalDate::class.java),
            dueDate = rs.getObject("due_date", LocalDate::class.java),
            currencyCode = rs.getString("currency_code"),
            exchangeRate = rs.getBigDecimal("exchange_rate"),
            subtotal = rs.getBigDecimal("subtotal"),
            taxTotal = rs.getBigDecimal("tax_total"),
            discountTotal = rs.getBigDecimal("discount_total"),
            totalAmount = rs.getBigDecimal("total_amount"),
            amountPaid = rs.getBigDecimal("amount_paid"),
            outstandingBalance = rs.getBigDecimal("outstanding_balance"),
            notes = rs.getString("notes"),
            terms = rs.getString("terms"),
            customerSnapshot = rs.getString("customer_snapshot"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java),
            issuedAt = rs.getObject("issued_at", OffsetDateTime::class.java)
        )
    }

    fun save(invoice: Invoice): Invoice {
        val sql = """
            INSERT INTO invoices (id, organization_id, customer_id, invoice_number, status, issue_date, due_date, currency_code, exchange_rate, subtotal, tax_total, discount_total, total_amount, amount_paid, outstanding_balance, notes, terms, customer_snapshot, created_at, updated_at, issued_at)
            VALUES (:id, :organizationId, :customerId, :invoiceNumber, :status::invoice_status, :issueDate, :dueDate, :currencyCode, :exchangeRate, :subtotal, :taxTotal, :discountTotal, :totalAmount, :amountPaid, :outstandingBalance, :notes, :terms, :customerSnapshot::jsonb, :createdAt, :updatedAt, :issuedAt)
            ON CONFLICT (id) DO UPDATE SET
                status = EXCLUDED.status,
                issue_date = EXCLUDED.issue_date,
                due_date = EXCLUDED.due_date,
                currency_code = EXCLUDED.currency_code,
                exchange_rate = EXCLUDED.exchange_rate,
                subtotal = EXCLUDED.subtotal,
                tax_total = EXCLUDED.tax_total,
                discount_total = EXCLUDED.discount_total,
                total_amount = EXCLUDED.total_amount,
                amount_paid = EXCLUDED.amount_paid,
                outstanding_balance = EXCLUDED.outstanding_balance,
                notes = EXCLUDED.notes,
                terms = EXCLUDED.terms,
                customer_snapshot = EXCLUDED.customer_snapshot,
                updated_at = EXCLUDED.updated_at,
                issued_at = EXCLUDED.issued_at
        """
        val params = MapSqlParameterSource()
            .addValue("id", invoice.id)
            .addValue("organizationId", invoice.organizationId)
            .addValue("customerId", invoice.customerId)
            .addValue("invoiceNumber", invoice.invoiceNumber)
            .addValue("status", invoice.status.name)
            .addValue("issueDate", invoice.issueDate)
            .addValue("dueDate", invoice.dueDate)
            .addValue("currencyCode", invoice.currencyCode)
            .addValue("exchangeRate", invoice.exchangeRate)
            .addValue("subtotal", invoice.subtotal)
            .addValue("taxTotal", invoice.taxTotal)
            .addValue("discountTotal", invoice.discountTotal)
            .addValue("totalAmount", invoice.totalAmount)
            .addValue("amountPaid", invoice.amountPaid)
            .addValue("outstandingBalance", invoice.outstandingBalance)
            .addValue("notes", invoice.notes)
            .addValue("terms", invoice.terms)
            .addValue("customerSnapshot", invoice.customerSnapshot)
            .addValue("createdAt", invoice.createdAt)
            .addValue("updatedAt", invoice.updatedAt)
            .addValue("issuedAt", invoice.issuedAt)
        jdbcTemplate.update(sql, params)
        return invoice
    }

    fun findByIdAndOrganizationId(id: UUID, organizationId: UUID): Invoice? {
        val sql = "SELECT * FROM invoices WHERE id = :id AND organization_id = :organizationId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("id", id).addValue("organizationId", organizationId), rowMapper).firstOrNull()
    }
    
    fun findAllByOrganizationId(organizationId: UUID): List<Invoice> {
        val sql = "SELECT * FROM invoices WHERE organization_id = :orgId ORDER BY created_at DESC"
        return jdbcTemplate.query(sql, MapSqlParameterSource("orgId", organizationId), rowMapper)
    }

    fun getNextInvoiceNumber(organizationId: UUID, year: Int): String {
        val sql = "SELECT COUNT(*) FROM invoices WHERE organization_id = :orgId AND EXTRACT(YEAR FROM created_at) = :year"
        val count = jdbcTemplate.queryForObject(sql, MapSqlParameterSource("orgId", organizationId).addValue("year", year), Int::class.java) ?: 0
        return "INV-$year-${String.format("%04d", count + 1)}"
    }
}

@Repository
class InvoiceLineRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun saveAll(lines: List<InvoiceLine>) {
        if (lines.isEmpty()) return
        val sql = """
            INSERT INTO invoice_lines (id, invoice_id, organization_id, line_number, description, quantity, unit_price, tax_rate, tax_amount, subtotal, total_amount, created_at)
            VALUES (:id, :invoiceId, :organizationId, :lineNumber, :description, :quantity, :unitPrice, :taxRate, :taxAmount, :subtotal, :totalAmount, :createdAt)
        """
        val batchParams = lines.map { line ->
            MapSqlParameterSource()
                .addValue("id", line.id)
                .addValue("invoiceId", line.invoiceId)
                .addValue("organizationId", line.organizationId)
                .addValue("lineNumber", line.lineNumber)
                .addValue("description", line.description)
                .addValue("quantity", line.quantity)
                .addValue("unitPrice", line.unitPrice)
                .addValue("taxRate", line.taxRate)
                .addValue("taxAmount", line.taxAmount)
                .addValue("subtotal", line.subtotal)
                .addValue("totalAmount", line.totalAmount)
                .addValue("createdAt", line.createdAt)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }

    fun deleteByInvoiceId(invoiceId: UUID) {
        jdbcTemplate.update("DELETE FROM invoice_lines WHERE invoice_id = :invoiceId", MapSqlParameterSource("invoiceId", invoiceId))
    }
    
    fun findByInvoiceId(invoiceId: UUID): List<InvoiceLine> {
        val sql = "SELECT * FROM invoice_lines WHERE invoice_id = :invoiceId ORDER BY line_number"
        return jdbcTemplate.query(sql, MapSqlParameterSource("invoiceId", invoiceId)) { rs, _ ->
            InvoiceLine(
                id = UUID.fromString(rs.getString("id")),
                invoiceId = UUID.fromString(rs.getString("invoice_id")),
                organizationId = UUID.fromString(rs.getString("organization_id")),
                lineNumber = rs.getInt("line_number"),
                description = rs.getString("description"),
                quantity = rs.getBigDecimal("quantity"),
                unitPrice = rs.getBigDecimal("unit_price"),
                taxRate = rs.getBigDecimal("tax_rate"),
                taxAmount = rs.getBigDecimal("tax_amount"),
                subtotal = rs.getBigDecimal("subtotal"),
                totalAmount = rs.getBigDecimal("total_amount"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }
}

@Repository
class InvoiceOutboxRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun save(outbox: InvoiceOutbox) {
        val sql = """
            INSERT INTO invoice_outbox (id, event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload, created_at, published_at, attempt_count, last_error)
            VALUES (:id, :eventId, :eventType, :aggregateType, :aggregateId, :tenantId, :routingKey, :payload, :createdAt, :publishedAt, :attemptCount, :lastError)
            ON CONFLICT (id) DO UPDATE SET
                published_at = EXCLUDED.published_at,
                attempt_count = EXCLUDED.attempt_count,
                last_error = EXCLUDED.last_error
        """
        val params = MapSqlParameterSource()
            .addValue("id", outbox.id)
            .addValue("eventId", outbox.eventId)
            .addValue("eventType", outbox.eventType)
            .addValue("aggregateType", outbox.aggregateType)
            .addValue("aggregateId", outbox.aggregateId)
            .addValue("tenantId", outbox.tenantId)
            .addValue("routingKey", outbox.routingKey)
            .addValue("payload", outbox.payload)
            .addValue("createdAt", outbox.createdAt)
            .addValue("publishedAt", outbox.publishedAt)
            .addValue("attemptCount", outbox.attemptCount)
            .addValue("lastError", outbox.lastError)
        jdbcTemplate.update(sql, params)
    }

    fun findUnpublished(limit: Int): List<InvoiceOutbox> {
        val sql = "SELECT * FROM invoice_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT :limit"
        return jdbcTemplate.query(sql, MapSqlParameterSource("limit", limit)) { rs, _ ->
            InvoiceOutbox(
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
    }
}
