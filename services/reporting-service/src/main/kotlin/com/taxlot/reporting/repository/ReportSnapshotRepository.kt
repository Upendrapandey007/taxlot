package com.taxlot.reporting.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.reporting.domain.*
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class ReportSnapshotRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper
) {

    fun saveSnapshot(snapshot: FinancialReportSnapshot): FinancialReportSnapshot {
        val sql = """
            INSERT INTO financial_report_snapshots (
                id, organization_id, report_type, period_id, from_date, to_date, currency_code, report_data, generated_by, created_at
            ) VALUES (
                :id, :organizationId, :reportType, :periodId, :fromDate, :toDate, :currencyCode, :reportData::jsonb, :generatedBy, :createdAt
            )
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", snapshot.id)
            .addValue("organizationId", snapshot.organizationId)
            .addValue("reportType", snapshot.reportType.name)
            .addValue("periodId", snapshot.periodId)
            .addValue("fromDate", snapshot.fromDate)
            .addValue("toDate", snapshot.toDate)
            .addValue("currencyCode", snapshot.currencyCode)
            .addValue("reportData", snapshot.reportData)
            .addValue("generatedBy", snapshot.generatedBy)
            .addValue("createdAt", Timestamp.from(snapshot.createdAt.toInstant()))

        jdbcTemplate.update(sql, params)
        return snapshot
    }

    fun findSnapshots(
        organizationId: UUID,
        reportType: ReportType? = null,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null
    ): List<FinancialReportSnapshot> {
        val conditions = mutableListOf("organization_id = :organizationId")
        val params = MapSqlParameterSource().addValue("organizationId", organizationId)

        if (reportType != null) {
            conditions.add("report_type = :reportType")
            params.addValue("reportType", reportType.name)
        }
        if (fromDate != null) {
            conditions.add("from_date >= :fromDate")
            params.addValue("fromDate", fromDate)
        }
        if (toDate != null) {
            conditions.add("to_date <= :toDate")
            params.addValue("toDate", toDate)
        }

        val sql = """
            SELECT id, organization_id, report_type, period_id, from_date, to_date, currency_code, report_data, generated_by, created_at
            FROM financial_report_snapshots
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY created_at DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, params) { rs: ResultSet, _: Int ->
            FinancialReportSnapshot(
                id = rs.getObject("id", UUID::class.java),
                organizationId = rs.getObject("organization_id", UUID::class.java),
                reportType = ReportType.valueOf(rs.getString("report_type")),
                periodId = rs.getObject("period_id", UUID::class.java),
                fromDate = rs.getDate("from_date").toLocalDate(),
                toDate = rs.getDate("to_date").toLocalDate(),
                currencyCode = rs.getString("currency_code"),
                reportData = rs.getString("report_data"),
                generatedBy = rs.getObject("generated_by", UUID::class.java),
                createdAt = rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC)
            )
        }
    }

    fun saveKpiSnapshot(kpi: FinancialKpiSummary): FinancialKpiSummary {
        val sql = """
            INSERT INTO financial_kpi_snapshots (
                id, organization_id, as_of_date, gross_profit_margin, net_profit_margin,
                current_ratio, quick_ratio, total_revenue, total_expenses, net_income,
                cash_balance, ar_outstanding, ap_outstanding, created_at
            ) VALUES (
                :id, :organizationId, :asOfDate, :grossProfitMargin, :netProfitMargin,
                :currentRatio, :quickRatio, :totalRevenue, :totalExpenses, :netIncome,
                :cashBalance, :arOutstanding, :apOutstanding, :createdAt
            )
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", UUID.randomUUID())
            .addValue("organizationId", kpi.organizationId)
            .addValue("asOfDate", kpi.asOfDate)
            .addValue("grossProfitMargin", kpi.grossProfitMargin)
            .addValue("netProfitMargin", kpi.netProfitMargin)
            .addValue("currentRatio", kpi.currentRatio)
            .addValue("quickRatio", kpi.quickRatio)
            .addValue("totalRevenue", kpi.totalRevenue)
            .addValue("totalExpenses", kpi.totalExpenses)
            .addValue("netIncome", kpi.netIncome)
            .addValue("cashBalance", kpi.cashBalance)
            .addValue("arOutstanding", kpi.arOutstanding)
            .addValue("apOutstanding", kpi.apOutstanding)
            .addValue("createdAt", Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()))

        jdbcTemplate.update(sql, params)
        return kpi
    }

    fun findLatestKpis(organizationId: UUID): FinancialKpiSummary? {
        val sql = """
            SELECT organization_id, as_of_date, gross_profit_margin, net_profit_margin,
                   current_ratio, quick_ratio, total_revenue, total_expenses, net_income,
                   cash_balance, ar_outstanding, ap_outstanding
            FROM financial_kpi_snapshots
            WHERE organization_id = :organizationId
            ORDER BY as_of_date DESC, created_at DESC
            LIMIT 1
        """.trimIndent()

        val params = MapSqlParameterSource().addValue("organizationId", organizationId)
        val list = jdbcTemplate.query(sql, params) { rs: ResultSet, _: Int ->
            FinancialKpiSummary(
                organizationId = rs.getObject("organization_id", UUID::class.java),
                asOfDate = rs.getDate("as_of_date").toLocalDate(),
                totalRevenue = rs.getBigDecimal("total_revenue"),
                totalExpenses = rs.getBigDecimal("total_expenses"),
                netIncome = rs.getBigDecimal("net_income"),
                grossProfitMargin = rs.getBigDecimal("gross_profit_margin") ?: java.math.BigDecimal.ZERO,
                netProfitMargin = rs.getBigDecimal("net_profit_margin") ?: java.math.BigDecimal.ZERO,
                currentRatio = rs.getBigDecimal("current_ratio") ?: java.math.BigDecimal.ZERO,
                quickRatio = rs.getBigDecimal("quick_ratio") ?: java.math.BigDecimal.ZERO,
                cashBalance = rs.getBigDecimal("cash_balance"),
                arOutstanding = rs.getBigDecimal("ar_outstanding"),
                apOutstanding = rs.getBigDecimal("ap_outstanding")
            )
        }
        return list.firstOrNull()
    }
}
