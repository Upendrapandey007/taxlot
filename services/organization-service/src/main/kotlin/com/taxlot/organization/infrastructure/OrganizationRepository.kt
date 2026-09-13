package com.taxlot.organization.infrastructure

import com.taxlot.organization.domain.IndustryType
import com.taxlot.organization.domain.Organization
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface OrganizationRepository {
    fun save(org: Organization): Organization
    fun findById(id: UUID): Organization?
    fun findByMemberId(userId: UUID): List<Organization>
    fun update(org: Organization): Organization
}

@Repository
class OrganizationRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : OrganizationRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Organization(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("name"),
            industry = IndustryType.valueOf(rs.getString("industry")),
            taxNumber = rs.getString("tax_number"),
            countryCode = rs.getString("country_code"),
            currencyCode = rs.getString("currency_code"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    override fun save(org: Organization): Organization {
        val sql = """
            INSERT INTO organizations (id, name, industry, tax_number, country_code, currency_code, is_active, created_at, updated_at)
            VALUES (:id, :name, :industry::industry_type, :taxNumber, :countryCode, :currencyCode, :isActive, :createdAt, :updatedAt)
        """
        val params = MapSqlParameterSource()
            .addValue("id", org.id)
            .addValue("name", org.name)
            .addValue("industry", org.industry.name)
            .addValue("taxNumber", org.taxNumber)
            .addValue("countryCode", org.countryCode)
            .addValue("currencyCode", org.currencyCode)
            .addValue("isActive", org.isActive)
            .addValue("createdAt", org.createdAt)
            .addValue("updatedAt", org.updatedAt)

        jdbcTemplate.update(sql, params)
        return org
    }

    override fun findById(id: UUID): Organization? {
        val sql = "SELECT * FROM organizations WHERE id = :id"
        return jdbcTemplate.query(sql, MapSqlParameterSource("id", id), rowMapper).firstOrNull()
    }

    override fun findByMemberId(userId: UUID): List<Organization> {
        val sql = """
            SELECT o.* FROM organizations o
            JOIN organization_members om ON o.id = om.organization_id
            WHERE om.user_id = :userId AND o.is_active = true AND om.is_active = true
        """
        return jdbcTemplate.query(sql, MapSqlParameterSource("userId", userId), rowMapper)
    }

    override fun update(org: Organization): Organization {
        val sql = """
            UPDATE organizations
            SET name = :name, industry = :industry::industry_type, tax_number = :taxNumber, 
                country_code = :countryCode, currency_code = :currencyCode, is_active = :isActive, updated_at = :updatedAt
            WHERE id = :id
        """
        val params = MapSqlParameterSource()
            .addValue("id", org.id)
            .addValue("name", org.name)
            .addValue("industry", org.industry.name)
            .addValue("taxNumber", org.taxNumber)
            .addValue("countryCode", org.countryCode)
            .addValue("currencyCode", org.currencyCode)
            .addValue("isActive", org.isActive)
            .addValue("updatedAt", OffsetDateTime.now())

        jdbcTemplate.update(sql, params)
        return findById(org.id) ?: org
    }
}
