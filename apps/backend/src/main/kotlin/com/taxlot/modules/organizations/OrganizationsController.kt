package com.taxlot.modules.organizations

import com.taxlot.modules.organizations.dto.CreateOrganizationRequest
import com.taxlot.modules.organizations.dto.OrganizationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Endpoints for organizations management")
class OrganizationsController(
    private val organizationService: OrganizationService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new organization")
    fun create(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: CreateOrganizationRequest
    ): OrganizationResponse {
        return organizationService.create(request, UUID.fromString(userId))
    }

    @GetMapping
    @Operation(summary = "List user's organizations")
    fun list(@AuthenticationPrincipal userId: String): List<OrganizationResponse> {
        return organizationService.listForUser(UUID.fromString(userId))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    fun getById(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: UUID
    ): OrganizationResponse {
        return organizationService.getById(id, UUID.fromString(userId))
    }
}
