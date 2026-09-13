package com.taxlot.organization.domain

import java.time.OffsetDateTime
import java.util.UUID

enum class IndustryType {
    RETAIL, AGENCY, FREELANCER, RESTAURANT, SERVICE, OTHER
}

enum class MemberRole {
    OWNER, ADMIN, ACCOUNTANT, STAFF, AUDITOR
}

enum class InvitationStatus {
    PENDING, ACCEPTED, EXPIRED, CANCELLED
}

data class Organization(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val industry: IndustryType = IndustryType.OTHER,
    val taxNumber: String? = null,
    val countryCode: String = "US",
    val currencyCode: String = "USD",
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)

data class OrganizationMember(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val userId: UUID,
    val role: MemberRole = MemberRole.STAFF,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class OrganizationInvitation(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val invitedEmail: String,
    val invitedBy: UUID,
    val role: MemberRole = MemberRole.STAFF,
    val tokenHash: String,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val expiresAt: OffsetDateTime,
    val acceptedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class OrganizationSettings(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val fiscalYearStart: Int = 1,
    val invoicePrefix: String = "INV",
    val invoiceNextNumber: Int = 1,
    val expenseApproval: Boolean = false,
    val timezone: String = "UTC",
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
