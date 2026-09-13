package com.taxlot.organization.infrastructure

import com.taxlot.organization.domain.OrganizationSettings
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface SettingsRepository {
    fun save(settings: OrganizationSettings): OrganizationSettings
    fun findByOrgId(orgId: UUID): OrganizationSettings?
    fun update(settings: OrganizationSettings): OrganizationSettings
}

@Repository
class SettingsRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SettingsRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        OrganizationSettings(
            id = UUID.fromString(rs.getString("id")),
            organizationId = UUID.fromString(rs.getString("organization_id")),
            fiscalYearStart = rs.getInt("fiscal_year_start"),
            invoicePrefix = rs.getString("invoice_prefix"),
            invoiceNextNumber = rs.getInt("invoice_next_number"),
            expenseApproval = rs.getBoolean("expense_approval"),
            timezone = rs.getString("timezone"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    override fun save(settings: OrganizationSettings): OrganizationSettings {
        val sql = """
            INSERT INTO organization_settings (id, organization_id, fiscal_year_start, invoice_prefix, invoice_next_number, expense_approval, timezone, created_at, updated_at)
            VALUES (:id, :orgId, :fys, :ip, :inn, :ea, :tz, :c, :u)
        """
        val params = MapSqlParameterSource()
            .addValue("id", settings.id)
            .addValue("orgId", settings.organizationId)
            .addValue("fys", settings.fiscalYearStart)
            .addValue("ip", settings.invoicePrefix)
            .addValue("inn", settings.invoiceNextNumber)
            .addValue("ea", settings.expenseApproval)
            .addValue("tz", settings.timezone)
            .addValue("c", settings.createdAt)
            .addValue("u", settings.updatedAt)

        jdbcTemplate.update(sql, params)
        return settings
    }

    override fun findByOrgId(orgId: UUID): OrganizationSettings? {
        val sql = "SELECT * FROM organization_settings WHERE organization_id = :orgId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("orgId", orgId), rowMapper).firstOrNull()
    }

    override fun update(settings: OrganizationSettings): OrganizationSettings {
        val sql = """
            UPDATE organization_settings
            SET fiscal_year_start = :fys, invoice_prefix = :ip, invoice_next_number = :inn, 
                expense_approval = :ea, timezone = :tz, updated_at = :u
            WHERE organization_id = :orgId
        """
        val params = MapSqlParameterSource()
            .addValue("orgId", settings.organizationId)
            .addValue("fys", settings.fiscalYearStart)
            .addValue("ip", settings.invoicePrefix)
            .addValue("inn", settings.invoiceNextNumber)
            .addValue("ea", settings.expenseApproval)
            .addValue("tz", settings.timezone)
            .addValue("u", OffsetDateTime.now())

        jdbcTemplate.update(sql, params)
        return findByOrgId(settings.organizationId) ?: settings
    }
}
