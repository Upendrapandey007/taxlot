package com.taxlot.organization.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.platform.error.BusinessRuleException
import com.taxlot.platform.event.EventEnvelope
import com.taxlot.platform.outbox.OutboxRecord
import com.taxlot.organization.domain.InvitationStatus
import com.taxlot.organization.domain.MemberRole
import com.taxlot.organization.domain.OrganizationInvitation
import com.taxlot.organization.infrastructure.InvitationRepository
import com.taxlot.organization.infrastructure.OrganizationMemberRepository
import com.taxlot.organization.infrastructure.OrgOutboxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class InviteMemberUseCase(
    private val invitationRepo: InvitationRepository,
    private val permissionChecker: PermissionChecker,
    private val outboxRepo: OrgOutboxRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun execute(userId: UUID, orgId: UUID, req: InviteRequest): InvitationResponse {
        permissionChecker.requirePermission(userId, orgId, "member.invite")
        
        val role = MemberRole.valueOf(req.role)
        val invitation = OrganizationInvitation(
            organizationId = orgId,
            invitedEmail = req.email,
            invitedBy = userId,
            role = role,
            tokenHash = UUID.randomUUID().toString(), // simplified token
            expiresAt = OffsetDateTime.now().plusDays(7)
        )
        val saved = invitationRepo.save(invitation)

        val event = EventEnvelope(
            eventType = "MemberInvited",
            producer = "organization-service",
            tenantId = orgId,
            aggregateType = "OrganizationInvitation",
            aggregateId = saved.id,
            payload = mapOf(
                "invitationId" to saved.id.toString(),
                "organizationId" to orgId.toString(),
                "email" to req.email,
                "role" to req.role,
                "token" to saved.tokenHash
            )
        )
        
        val record = OutboxRecord(
            eventId = event.eventId,
            eventType = event.eventType,
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId,
            tenantId = event.tenantId,
            routingKey = "organization.member.invited",
            payload = objectMapper.writeValueAsString(event),
            createdAt = OffsetDateTime.now()
        )
        outboxRepo.save(record)

        return InvitationResponse(saved.id, saved.invitedEmail, saved.role.name, saved.status.name)
    }
}

@Service
class ListMembersUseCase(
    private val memberRepo: OrganizationMemberRepository,
    private val permissionChecker: PermissionChecker
) {
    fun execute(userId: UUID, orgId: UUID): List<MemberResponse> {
        permissionChecker.requirePermission(userId, orgId, "member.read")
        return memberRepo.findByOrgId(orgId).map { 
            MemberResponse(it.id, it.userId, it.role.name, it.isActive) 
        }
    }
}

@Service
class RemoveMemberUseCase(
    private val memberRepo: OrganizationMemberRepository,
    private val permissionChecker: PermissionChecker
) {
    @Transactional
    fun execute(userId: UUID, orgId: UUID, targetUserId: UUID) {
        permissionChecker.requirePermission(userId, orgId, "member.remove")
        
        val targetRole = memberRepo.findRole(orgId, targetUserId)
            ?: throw BusinessRuleException("MEMBER_NOT_FOUND", "User is not a member of this organization")
            
        if (targetRole == MemberRole.OWNER) {
            val ownerCount = memberRepo.countOwners(orgId)
            if (ownerCount <= 1) {
                throw BusinessRuleException("LAST_OWNER", "Cannot remove the last owner of the organization")
            }
        }
        
        memberRepo.removeMember(orgId, targetUserId)
    }
}

data class InviteRequest(val email: String, val role: String)
data class InvitationResponse(val id: UUID, val email: String, val role: String, val status: String)
data class MemberResponse(val id: UUID, val userId: UUID, val role: String, val isActive: Boolean)
