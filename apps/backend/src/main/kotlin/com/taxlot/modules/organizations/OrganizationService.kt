package com.taxlot.modules.organizations

import com.taxlot.common.exception.ForbiddenException
import com.taxlot.common.exception.NotFoundException
import com.taxlot.modules.organizations.dto.CreateOrganizationRequest
import com.taxlot.modules.organizations.dto.OrganizationResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository
) {
    @Transactional
    fun create(request: CreateOrganizationRequest, ownerId: UUID): OrganizationResponse {
        val org = Organization(
            name = request.name,
            industry = request.industry,
            taxNumber = request.taxNumber,
            countryCode = request.countryCode,
            currencyCode = request.currencyCode
        )
        val savedOrg = organizationRepository.save(org)
        organizationRepository.addMember(savedOrg.id, ownerId, MemberRole.OWNER)

        return mapToResponse(savedOrg)
    }

    fun listForUser(userId: UUID): List<OrganizationResponse> {
        return organizationRepository.findByMemberId(userId).map { mapToResponse(it) }
    }

    fun getById(orgId: UUID, requestingUserId: UUID): OrganizationResponse {
        val role = organizationRepository.findMemberRole(orgId, requestingUserId)
            ?: throw ForbiddenException("You do not have access to this organization")

        val org = organizationRepository.findById(orgId)
            ?: throw NotFoundException("Organization not found")

        return mapToResponse(org)
    }

    private fun mapToResponse(org: Organization) = OrganizationResponse(
        id = org.id,
        name = org.name,
        industry = org.industry,
        taxNumber = org.taxNumber,
        countryCode = org.countryCode,
        currencyCode = org.currencyCode,
        createdAt = org.createdAt
    )
}
