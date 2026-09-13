package com.taxlot.organization.infrastructure

import com.taxlot.organization.domain.MemberRole
import com.taxlot.organization.domain.OrganizationMember
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface OrganizationMemberRepository {
    fun save(member: OrganizationMember): OrganizationMember
    fun findByOrgId(orgId: UUID): List<OrganizationMember>
    fun findRole(orgId: UUID, userId: UUID): MemberRole?
    fun existsAsOwner(orgId: UUID, userId: UUID): Boolean
    fun removeMember(orgId: UUID, userId: UUID)
    fun countOwners(orgId: UUID): Int
}

@Repository
class OrganizationMemberRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : OrganizationMemberRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        OrganizationMember(
            id = UUID.fromString(rs.getString("id")),
            organizationId = UUID.fromString(rs.getString("organization_id")),
            userId = UUID.fromString(rs.getString("user_id")),
            role = MemberRole.valueOf(rs.getString("role")),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
        )
    }

    override fun save(member: OrganizationMember): OrganizationMember {
        val sql = """
            INSERT INTO organization_members (id, organization_id, user_id, role, is_active, created_at)
            VALUES (:id, :orgId, :userId, :role::member_role, :isActive, :createdAt)
        """
        val params = MapSqlParameterSource()
            .addValue("id", member.id)
            .addValue("orgId", member.organizationId)
            .addValue("userId", member.userId)
            .addValue("role", member.role.name)
            .addValue("isActive", member.isActive)
            .addValue("createdAt", member.createdAt)

        jdbcTemplate.update(sql, params)
        return member
    }

    override fun findByOrgId(orgId: UUID): List<OrganizationMember> {
        val sql = "SELECT * FROM organization_members WHERE organization_id = :orgId AND is_active = true"
        return jdbcTemplate.query(sql, MapSqlParameterSource("orgId", orgId), rowMapper)
    }

    override fun findRole(orgId: UUID, userId: UUID): MemberRole? {
        val sql = "SELECT role FROM organization_members WHERE organization_id = :orgId AND user_id = :userId AND is_active = true"
        val params = MapSqlParameterSource().addValue("orgId", orgId).addValue("userId", userId)
        return jdbcTemplate.query(sql, params) { rs, _ -> MemberRole.valueOf(rs.getString("role")) }.firstOrNull()
    }

    override fun existsAsOwner(orgId: UUID, userId: UUID): Boolean {
        return findRole(orgId, userId) == MemberRole.OWNER
    }
    
    override fun removeMember(orgId: UUID, userId: UUID) {
        val sql = "DELETE FROM organization_members WHERE organization_id = :orgId AND user_id = :userId"
        jdbcTemplate.update(sql, MapSqlParameterSource().addValue("orgId", orgId).addValue("userId", userId))
    }
    
    override fun countOwners(orgId: UUID): Int {
        val sql = "SELECT count(*) FROM organization_members WHERE organization_id = :orgId AND role = 'OWNER' AND is_active = true"
        return jdbcTemplate.queryForObject(sql, MapSqlParameterSource("orgId", orgId), Int::class.java) ?: 0
    }
}
