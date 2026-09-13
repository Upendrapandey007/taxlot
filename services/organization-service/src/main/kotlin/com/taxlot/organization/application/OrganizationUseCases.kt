package com.taxlot.organization.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.platform.error.NotFoundException
import com.taxlot.platform.event.EventEnvelope
import com.taxlot.platform.outbox.OutboxRecord
import com.taxlot.organization.domain.IndustryType
import com.taxlot.organization.domain.MemberRole
import com.taxlot.organization.domain.Organization
import com.taxlot.organization.domain.OrganizationMember
import com.taxlot.organization.domain.OrganizationSettings
import com.taxlot.organization.infrastructure.OrganizationMemberRepository
import com.taxlot.organization.infrastructure.OrganizationRepository
import com.taxlot.organization.infrastructure.OrgOutboxRepository
import com.taxlot.organization.infrastructure.SettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CreateOrganizationUseCase(
    private val orgRepo: OrganizationRepository,
    private val memberRepo: OrganizationMemberRepository,
    private val settingsRepo: SettingsRepository,
    private val outboxRepo: OrgOutboxRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun execute(userId: UUID, req: CreateOrganizationRequest): OrganizationResponse {
        val org = Organization(
            name = req.name,
            industry = IndustryType.valueOf(req.industry),
            taxNumber = req.taxNumber,
            countryCode = req.countryCode ?: "US",
            currencyCode = req.currencyCode ?: "USD"
        )
        val savedOrg = orgRepo.save(org)

        val owner = OrganizationMember(
            organizationId = savedOrg.id,
            userId = userId,
            role = MemberRole.OWNER
        )
        memberRepo.save(owner)

        val settings = OrganizationSettings(organizationId = savedOrg.id)
        settingsRepo.save(settings)

        val event = EventEnvelope(
            eventType = "OrganizationCreated",
            producer = "organization-service",
            tenantId = savedOrg.id,
            aggregateType = "Organization",
            aggregateId = savedOrg.id,
            payload = mapOf(
                "id" to savedOrg.id.toString(),
                "name" to savedOrg.name,
                "ownerUserId" to userId.toString()
            )
        )
        
        val record = OutboxRecord(
            eventId = event.eventId,
            eventType = event.eventType,
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId,
            tenantId = event.tenantId,
            routingKey = "organization.organization.created",
            payload = objectMapper.writeValueAsString(event),
            createdAt = OffsetDateTime.now()
        )
        outboxRepo.save(record)

        return mapToResponse(savedOrg)
    }
}

@Service
class GetOrganizationUseCase(
    private val orgRepo: OrganizationRepository,
    private val permissionChecker: PermissionChecker
) {
    fun execute(userId: UUID, id: UUID): OrganizationResponse {
        permissionChecker.requirePermission(userId, id, "organization.read")
        val org = orgRepo.findById(id) ?: throw NotFoundException("ORG_NOT_FOUND", "Organization not found")
        return mapToResponse(org)
    }
}

@Service
class UpdateOrganizationUseCase(
    private val orgRepo: OrganizationRepository,
    private val permissionChecker: PermissionChecker
) {
    @Transactional
    fun execute(userId: UUID, id: UUID, req: UpdateOrganizationRequest): OrganizationResponse {
        permissionChecker.requirePermission(userId, id, "organization.update")
        val org = orgRepo.findById(id) ?: throw NotFoundException("ORG_NOT_FOUND", "Organization not found")
        
        val updated = org.copy(
            name = req.name ?: org.name,
            industry = req.industry?.let { IndustryType.valueOf(it) } ?: org.industry,
            taxNumber = req.taxNumber ?: org.taxNumber,
            countryCode = req.countryCode ?: org.countryCode,
            currencyCode = req.currencyCode ?: org.currencyCode
        )
        val saved = orgRepo.update(updated)
        return mapToResponse(saved)
    }
}

@Service
class ListOrganizationsUseCase(
    private val orgRepo: OrganizationRepository
) {
    fun execute(userId: UUID): List<OrganizationResponse> {
        return orgRepo.findByMemberId(userId).map { mapToResponse(it) }
    }
}

data class CreateOrganizationRequest(
    val name: String,
    val industry: String,
    val taxNumber: String?,
    val countryCode: String?,
    val currencyCode: String?
)

data class UpdateOrganizationRequest(
    val name: String?,
    val industry: String?,
    val taxNumber: String?,
    val countryCode: String?,
    val currencyCode: String?
)

data class OrganizationResponse(
    val id: UUID,
    val name: String,
    val industry: String,
    val taxNumber: String?,
    val countryCode: String,
    val currencyCode: String,
    val isActive: Boolean
)

fun mapToResponse(org: Organization) = OrganizationResponse(
    id = org.id,
    name = org.name,
    industry = org.industry.name,
    taxNumber = org.taxNumber,
    countryCode = org.countryCode,
    currencyCode = org.currencyCode,
    isActive = org.isActive
)
