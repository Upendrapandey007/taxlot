package com.taxlot.modules.organizations

import java.time.OffsetDateTime
import java.util.UUID

enum class IndustryType {
    RETAIL, AGENCY, FREELANCER, RESTAURANT, SERVICE, OTHER
}

enum class MemberRole {
    OWNER, ADMIN, ACCOUNTANT, STAFF, AUDITOR
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
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
