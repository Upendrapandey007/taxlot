package com.taxlot.organization.application

import com.taxlot.platform.error.ForbiddenException
import com.taxlot.organization.domain.MemberRole
import com.taxlot.organization.infrastructure.OrganizationMemberRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PermissionChecker(private val memberRepository: OrganizationMemberRepository) {
    
    // We mock the role_permissions map here as instructed.
    private val rolePermissions = mapOf(
        MemberRole.OWNER to setOf(
            "organization.read", "organization.update", "member.read", "member.invite", "member.remove",
            "customer.read", "customer.create", "customer.update", "customer.delete",
            "invoice.read", "invoice.create", "invoice.issue", "invoice.cancel",
            "expense.read", "expense.create", "expense.approve", "payment.read", "payment.create",
            "accounting.read", "accounting.post", "report.read", "tax.read", "tax.manage"
        ),
        MemberRole.ADMIN to setOf(
            "organization.read", "organization.update", "member.read", "member.invite", "member.remove",
            "customer.read", "customer.create", "customer.update", "customer.delete",
            "invoice.read", "invoice.create", "invoice.issue", "invoice.cancel",
            "expense.read", "expense.create", "expense.approve", "payment.read", "payment.create",
            "accounting.read", "accounting.post", "report.read", "tax.read", "tax.manage"
        ),
        MemberRole.ACCOUNTANT to setOf(
            "organization.read", "customer.read", "customer.create", "customer.update",
            "invoice.read", "invoice.create", "invoice.issue", "invoice.cancel",
            "expense.read", "expense.approve", "payment.read", "payment.create",
            "accounting.read", "accounting.post", "report.read", "tax.read", "tax.manage"
        ),
        MemberRole.STAFF to setOf(
            "organization.read", "customer.read", "customer.create", "customer.update",
            "invoice.read", "invoice.create", "expense.read", "expense.create", "payment.read"
        ),
        MemberRole.AUDITOR to setOf(
            "organization.read", "customer.read", "invoice.read", "expense.read", 
            "payment.read", "accounting.read", "report.read", "tax.read"
        )
    )

    fun requirePermission(userId: UUID, organizationId: UUID, permission: String) {
        val role = memberRepository.findRole(organizationId, userId)
            ?: throw ForbiddenException("ORG_ACCESS_DENIED", "Not a member of this organization")
            
        if (!roleHasPermission(role, permission)) {
            throw ForbiddenException("PERMISSION_DENIED", "Insufficient permissions: ${permission} required")
        }
    }
    
    fun isMember(userId: UUID, organizationId: UUID): Boolean =
        memberRepository.findRole(organizationId, userId) != null

    private fun roleHasPermission(role: MemberRole, permission: String): Boolean {
        return rolePermissions[role]?.contains(permission) ?: false
    }
}
