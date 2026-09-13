package com.taxlot.organization.api

import com.taxlot.organization.application.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationsController(
    private val createOrganizationUseCase: CreateOrganizationUseCase,
    private val getOrganizationUseCase: GetOrganizationUseCase,
    private val updateOrganizationUseCase: UpdateOrganizationUseCase,
    private val listOrganizationsUseCase: ListOrganizationsUseCase,
    private val inviteMemberUseCase: InviteMemberUseCase,
    private val listMembersUseCase: ListMembersUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrg(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: CreateOrganizationRequest
    ): OrganizationResponse {
        return createOrganizationUseCase.execute(userId, request)
    }

    @GetMapping
    fun listOrgs(@RequestHeader("X-User-Id") userId: UUID): List<OrganizationResponse> {
        return listOrganizationsUseCase.execute(userId)
    }

    @GetMapping("/{id}")
    fun getOrg(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID
    ): OrganizationResponse {
        return getOrganizationUseCase.execute(userId, id)
    }

    @PatchMapping("/{id}")
    fun updateOrg(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateOrganizationRequest
    ): OrganizationResponse {
        return updateOrganizationUseCase.execute(userId, id, request)
    }

    @GetMapping("/{id}/members")
    fun listMembers(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID
    ): List<MemberResponse> {
        return listMembersUseCase.execute(userId, id)
    }

    @DeleteMapping("/{id}/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable targetUserId: UUID
    ) {
        removeMemberUseCase.execute(userId, id, targetUserId)
    }

    @PostMapping("/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    fun inviteMember(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: InviteRequest
    ): InvitationResponse {
        return inviteMemberUseCase.execute(userId, id, request)
    }
    
    // NOTE: /invitations GET was missing in the use cases, skipped for brevity or add if needed

    @GetMapping("/{id}/settings")
    fun getSettings(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID
    ): OrganizationSettingsResponse {
        return getSettingsUseCase.execute(userId, id)
    }

    @PatchMapping("/{id}/settings")
    fun updateSettings(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateSettingsRequest
    ): OrganizationSettingsResponse {
        return updateSettingsUseCase.execute(userId, id, request)
    }
}
