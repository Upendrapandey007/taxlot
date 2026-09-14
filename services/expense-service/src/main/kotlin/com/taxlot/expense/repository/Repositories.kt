package com.taxlot.expense.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.expense.domain.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ExpenseRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun generateExpenseNumber(year: Int): String {
        val countSql = "SELECT COUNT(*) FROM expenses WHERE extract(year from created_at) = :year"
        val count = jdbcTemplate.queryForObject(
            countSql, 
            MapSqlParameterSource("year", year), 
            Int::class.java
        ) ?: 0
        return "EXP-$year-${String.format("%04d", count + 1)}"
    }

    fun save(expense: Expense): Expense {
        val sql = """
            INSERT INTO expenses (
                id, organization_id, expense_number, vendor_name, category, expense_date,
                payment_method, currency_code, exchange_rate, subtotal, tax_amount, total_amount,
                receipt_url, notes, status, created_by, created_at, updated_at
            ) VALUES (
                :id, :organizationId, :expenseNumber, :vendorName, :category::expense_category, :expenseDate,
                :paymentMethod::expense_payment_method, :currencyCode, :exchangeRate, :subtotal, :taxAmount, :totalAmount,
                :receiptUrl, :notes, :status::expense_status, :createdBy, :createdAt, :updatedAt
            )
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", expense.id)
            .addValue("organizationId", expense.organizationId)
            .addValue("expenseNumber", expense.expenseNumber)
            .addValue("vendorName", expense.vendorName)
            .addValue("category", expense.category.name)
            .addValue("expenseDate", expense.expenseDate)
            .addValue("paymentMethod", expense.paymentMethod.name)
            .addValue("currencyCode", expense.currencyCode)
            .addValue("exchangeRate", expense.exchangeRate)
            .addValue("subtotal", expense.subtotal)
            .addValue("taxAmount", expense.taxAmount)
            .addValue("totalAmount", expense.totalAmount)
            .addValue("receiptUrl", expense.receiptUrl)
            .addValue("notes", expense.notes)
            .addValue("status", expense.status.name)
            .addValue("createdBy", expense.createdBy)
            .addValue("createdAt", expense.createdAt)
            .addValue("updatedAt", expense.updatedAt)

        jdbcTemplate.update(sql, params)
        return expense
    }

    fun updateStatus(id: UUID, organizationId: UUID, status: ExpenseStatus) {
        val sql = """
            UPDATE expenses SET status = :status::expense_status, updated_at = :updatedAt
            WHERE id = :id AND organization_id = :organizationId
        """.trimIndent()
        jdbcTemplate.update(sql, MapSqlParameterSource()
            .addValue("id", id)
            .addValue("organizationId", organizationId)
            .addValue("status", status.name)
            .addValue("updatedAt", OffsetDateTime.now())
        )
    }

    fun findById(id: UUID, organizationId: UUID): Expense? {
        val sql = "SELECT * FROM expenses WHERE id = :id AND organization_id = :organizationId"
        return jdbcTemplate.query(sql, MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId)) { rs, _ ->
            mapRowToExpense(rs)
        }.firstOrNull()
    }

    fun findAll(organizationId: UUID, category: ExpenseCategory?, page: Int, size: Int): List<Expense> {
        var sql = "SELECT * FROM expenses WHERE organization_id = :organizationId"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId)
        
        if (category != null) {
            sql += " AND category = :category::expense_category"
            params.addValue("category", category.name)
        }
        
        sql += " ORDER BY created_at DESC LIMIT :limit OFFSET :offset"
        params.addValue("limit", size).addValue("offset", page * size)
        
        return jdbcTemplate.query(sql, params) { rs, _ -> mapRowToExpense(rs) }
    }

    private fun mapRowToExpense(rs: ResultSet): Expense {
        return Expense(
            id = rs.getObject("id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            expenseNumber = rs.getString("expense_number"),
            vendorName = rs.getString("vendor_name"),
            category = ExpenseCategory.valueOf(rs.getString("category")),
            expenseDate = rs.getDate("expense_date").toLocalDate(),
            paymentMethod = ExpensePaymentMethod.valueOf(rs.getString("payment_method")),
            currencyCode = rs.getString("currency_code"),
            exchangeRate = rs.getBigDecimal("exchange_rate"),
            subtotal = rs.getBigDecimal("subtotal"),
            taxAmount = rs.getBigDecimal("tax_amount"),
            totalAmount = rs.getBigDecimal("total_amount"),
            receiptUrl = rs.getString("receipt_url"),
            notes = rs.getString("notes"),
            status = ExpenseStatus.valueOf(rs.getString("status")),
            createdBy = rs.getObject("created_by", UUID::class.java),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }
}

@Repository
class ExpenseOutboxRepository(private val jdbcTemplate: NamedParameterJdbcTemplate, private val objectMapper: ObjectMapper) {
    fun save(eventId: UUID, eventType: String, aggregateType: String, aggregateId: UUID, tenantId: UUID, routingKey: String, payload: Any) {
        val sql = """
            INSERT INTO expense_outbox (event_id, event_type, aggregate_type, aggregate_id, tenant_id, routing_key, payload)
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
        val sql = "SELECT * FROM expense_outbox WHERE published_at IS NULL ORDER BY created_at ASC LIMIT 50"
        return jdbcTemplate.queryForList(sql, MapSqlParameterSource())
    }

    fun markPublished(id: UUID) {
        val sql = "UPDATE expense_outbox SET published_at = NOW() WHERE id = :id"
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id))
    }
}
