package com.taxlot.organization.infrastructure

import com.taxlot.organization.domain.InvitationStatus
import com.taxlot.organization.domain.MemberRole
import com.taxlot.organization.domain.OrganizationInvitation
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

interface InvitationRepository {
    fun save(invitation: OrganizationInvitation): OrganizationInvitation
    fun findByTokenHash(tokenHash: String): OrganizationInvitation?
    fun findByOrgId(orgId: UUID): List<OrganizationInvitation>
    fun updateStatus(id: UUID, status: InvitationStatus)
}

@Repository
class InvitationRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : InvitationRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        OrganizationInvitation(
            id = UUID.fromString(rs.getString("id")),
            organizationId = UUID.fromString(rs.getString("organization_id")),
            invitedEmail = rs.getString("invited_email"),
            invitedBy = UUID.fromString(rs.getString("invited_by")),
            role = MemberRole.valueOf(rs.getString("role")),
            tokenHash = rs.getString("token_hash"),
            status = InvitationStatus.valueOf(rs.getString("status")),
            expiresAt = rs.getObject("expires_at", OffsetDateTime::class.java),
            acceptedAt = rs.getObject("accepted_at", OffsetDateTime::class.java),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
        )
    }

    override fun save(invitation: OrganizationInvitation): OrganizationInvitation {
        val sql = """
            INSERT INTO organization_invitations (id, organization_id, invited_email, invited_by, role, token_hash, status, expires_at, accepted_at, created_at)
            VALUES (:id, :orgId, :email, :by, :role::member_role, :token, :status::invitation_status, :expires, :accepted, :created)
        """
        val params = MapSqlParameterSource()
            .addValue("id", invitation.id)
            .addValue("orgId", invitation.organizationId)
            .addValue("email", invitation.invitedEmail)
            .addValue("by", invitation.invitedBy)
            .addValue("role", invitation.role.name)
            .addValue("token", invitation.tokenHash)
            .addValue("status", invitation.status.name)
            .addValue("expires", invitation.expiresAt)
            .addValue("accepted", invitation.acceptedAt)
            .addValue("created", invitation.createdAt)

        jdbcTemplate.update(sql, params)
        return invitation
    }

    override fun findByTokenHash(tokenHash: String): OrganizationInvitation? {
        val sql = "SELECT * FROM organization_invitations WHERE token_hash = :token"
        return jdbcTemplate.query(sql, MapSqlParameterSource("token", tokenHash), rowMapper).firstOrNull()
    }

    override fun findByOrgId(orgId: UUID): List<OrganizationInvitation> {
        val sql = "SELECT * FROM organization_invitations WHERE organization_id = :orgId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("orgId", orgId), rowMapper)
    }

    override fun updateStatus(id: UUID, status: InvitationStatus) {
        val sql = """
            UPDATE organization_invitations 
            SET status = :status::invitation_status, 
                accepted_at = CASE WHEN :status = 'ACCEPTED' THEN NOW() ELSE accepted_at END 
            WHERE id = :id
        """
        jdbcTemplate.update(sql, MapSqlParameterSource("id", id).addValue("status", status.name))
    }
}
