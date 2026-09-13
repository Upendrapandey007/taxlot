package com.taxlot.accounting.infrastructure

import com.taxlot.accounting.domain.JournalLine
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class JournalLineRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        JournalLine(
            id = rs.getObject("id", UUID::class.java),
            journalEntryId = rs.getObject("journal_entry_id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            accountId = rs.getObject("account_id", UUID::class.java),
            lineNumber = rs.getInt("line_number"),
            debit = rs.getBigDecimal("debit"),
            credit = rs.getBigDecimal("credit"),
            currencyCode = rs.getString("currency_code"),
            exchangeRate = rs.getBigDecimal("exchange_rate"),
            description = rs.getString("description"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
        )
    }

    fun saveLines(lines: List<JournalLine>) {
        val sql = """
            INSERT INTO journal_lines (
                id, journal_entry_id, organization_id, account_id, line_number, debit, credit, currency_code, exchange_rate, description, created_at
            ) VALUES (
                :id, :journalEntryId, :organizationId, :accountId, :lineNumber, :debit, :credit, :currencyCode, :exchangeRate, :description, :createdAt
            )
        """
        val batchParams = lines.map { line ->
            MapSqlParameterSource()
                .addValue("id", line.id)
                .addValue("journalEntryId", line.journalEntryId)
                .addValue("organizationId", line.organizationId)
                .addValue("accountId", line.accountId)
                .addValue("lineNumber", line.lineNumber)
                .addValue("debit", line.debit)
                .addValue("credit", line.credit)
                .addValue("currencyCode", line.currencyCode)
                .addValue("exchangeRate", line.exchangeRate)
                .addValue("description", line.description)
                .addValue("createdAt", line.createdAt)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }

    fun findByJournalEntryId(journalEntryId: UUID): List<JournalLine> {
        val sql = "SELECT * FROM journal_lines WHERE journal_entry_id = :journalEntryId ORDER BY line_number"
        val params = MapSqlParameterSource().addValue("journalEntryId", journalEntryId)
        return jdbcTemplate.query(sql, params, rowMapper)
    }

    fun findByAccountAndPeriod(organizationId: UUID, accountId: UUID, periodId: UUID): List<JournalLine> {
        val sql = """
            SELECT jl.* FROM journal_lines jl
            JOIN journal_entries je ON jl.journal_entry_id = je.id
            WHERE jl.organization_id = :organizationId 
              AND jl.account_id = :accountId 
              AND je.period_id = :periodId
            ORDER BY je.posted_at, jl.line_number
        """
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("accountId", accountId)
            .addValue("periodId", periodId)
        return jdbcTemplate.query(sql, params, rowMapper)
    }
}
