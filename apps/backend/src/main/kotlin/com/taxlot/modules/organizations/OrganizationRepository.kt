package com.taxlot.modules.organizations

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class OrganizationRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) {
    private val orgRowMapper = RowMapper { rs: ResultSet, _: Int ->
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

    fun save(org: Organization): Organization {
        val sql = """
            INSERT INTO organizations (id, name, industry, tax_number, country_code, currency_code, is_active, created_at, updated_at)
            VALUES (:id, :name, :industry::industry_type, :taxNumber, :countryCode, :currencyCode, :isActive, :createdAt, :updatedAt)
            RETURNING *
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("id", org.id)
            .addValue("name", org.name)
            .addValue("industry", org.industry.name)
            .addValue("taxNumber", org.taxNumber)
            .addValue("countryCode", org.countryCode)
            .addValue("currencyCode", org.currencyCode)
            .addValue("isActive", org.isActive)
            .addValue("createdAt", org.createdAt)
            .addValue("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))

        return jdbcTemplate.queryForObject(sql, params, orgRowMapper)!!
    }

    fun findById(id: UUID): Organization? {
        val sql = "SELECT * FROM organizations WHERE id = :id"
        return jdbcTemplate.query(sql, MapSqlParameterSource("id", id), orgRowMapper).firstOrNull()
    }

    fun findByMemberId(userId: UUID): List<Organization> {
        val sql = """
            SELECT o.* FROM organizations o
            JOIN organization_members om ON o.id = om.organization_id
            WHERE om.user_id = :userId
        """.trimIndent()
        return jdbcTemplate.query(sql, MapSqlParameterSource("userId", userId), orgRowMapper)
    }

    fun addMember(orgId: UUID, userId: UUID, role: MemberRole) {
        val sql = """
            INSERT INTO organization_members (organization_id, user_id, role)
            VALUES (:orgId, :userId, :role::member_role)
            ON CONFLICT (organization_id, user_id) DO UPDATE SET role = :role::member_role
        """.trimIndent()
        val params = MapSqlParameterSource()
            .addValue("orgId", orgId)
            .addValue("userId", userId)
            .addValue("role", role.name)
        jdbcTemplate.update(sql, params)
    }

    fun findMemberRole(orgId: UUID, userId: UUID): MemberRole? {
        val sql = "SELECT role FROM organization_members WHERE organization_id = :orgId AND user_id = :userId"
        val params = MapSqlParameterSource()
            .addValue("orgId", orgId)
            .addValue("userId", userId)
        val roles = jdbcTemplate.query(sql, params) { rs, _ -> MemberRole.valueOf(rs.getString("role")) }
        return roles.firstOrNull()
    }
}
