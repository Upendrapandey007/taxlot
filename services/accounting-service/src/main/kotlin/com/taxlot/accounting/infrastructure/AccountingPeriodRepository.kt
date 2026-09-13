package com.taxlot.accounting.infrastructure

import com.taxlot.accounting.domain.AccountingPeriod
import com.taxlot.accounting.domain.PeriodStatus
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class AccountingPeriodRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        AccountingPeriod(
            id = rs.getObject("id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            name = rs.getString("name"),
            startDate = rs.getObject("start_date", LocalDate::class.java),
            endDate = rs.getObject("end_date", LocalDate::class.java),
            status = PeriodStatus.valueOf(rs.getString("status")),
            lockedAt = rs.getObject("locked_at", OffsetDateTime::class.java),
            lockedBy = rs.getObject("locked_by", UUID::class.java),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
        )
    }

    fun save(period: AccountingPeriod): AccountingPeriod {
        val sql = """
            INSERT INTO accounting_periods (
                id, organization_id, name, start_date, end_date, status, locked_at, locked_by, created_at
            ) VALUES (
                :id, :organizationId, :name, :startDate, :endDate, :status::period_status, :lockedAt, :lockedBy, :createdAt
            )
        """
        val params = MapSqlParameterSource()
            .addValue("id", period.id)
            .addValue("organizationId", period.organizationId)
            .addValue("name", period.name)
            .addValue("startDate", period.startDate)
            .addValue("endDate", period.endDate)
            .addValue("status", period.status.name)
            .addValue("lockedAt", period.lockedAt)
            .addValue("lockedBy", period.lockedBy)
            .addValue("createdAt", period.createdAt)
        jdbcTemplate.update(sql, params)
        return period
    }

    fun findById(id: UUID, organizationId: UUID): AccountingPeriod? {
        val sql = "SELECT * FROM accounting_periods WHERE id = :id AND organization_id = :organizationId"
        val params = MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun findPeriodForDate(organizationId: UUID, date: LocalDate): AccountingPeriod? {
        val sql = "SELECT * FROM accounting_periods WHERE organization_id = :organizationId AND start_date <= :date AND end_date >= :date"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId).addValue("date", date)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun listByOrganization(organizationId: UUID): List<AccountingPeriod> {
        val sql = "SELECT * FROM accounting_periods WHERE organization_id = :organizationId ORDER BY start_date DESC"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId)
        return jdbcTemplate.query(sql, params, rowMapper)
    }

    fun updateStatus(id: UUID, organizationId: UUID, status: PeriodStatus, lockedBy: UUID?) {
        val sql = "UPDATE accounting_periods SET status = :status::period_status, locked_at = :lockedAt, locked_by = :lockedBy WHERE id = :id AND organization_id = :organizationId"
        val params = MapSqlParameterSource()
            .addValue("id", id)
            .addValue("organizationId", organizationId)
            .addValue("status", status.name)
            .addValue("lockedAt", if (status == PeriodStatus.LOCKED) OffsetDateTime.now() else null)
            .addValue("lockedBy", lockedBy)
        jdbcTemplate.update(sql, params)
    }
}
