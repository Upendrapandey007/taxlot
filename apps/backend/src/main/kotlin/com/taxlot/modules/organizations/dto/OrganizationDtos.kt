package com.taxlot.modules.organizations.dto

import com.taxlot.modules.organizations.IndustryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime
import java.util.UUID

data class CreateOrganizationRequest(
    @field:NotBlank @field:Size(max = 255) val name: String,
    val industry: IndustryType = IndustryType.OTHER,
    val taxNumber: String? = null,
    @field:NotBlank @field:Size(min = 2, max = 2) val countryCode: String,
    @field:NotBlank @field:Size(min = 3, max = 3) val currencyCode: String
)

data class OrganizationResponse(
    val id: UUID,
    val name: String,
    val industry: IndustryType,
    val taxNumber: String?,
    val countryCode: String,
    val currencyCode: String,
    val createdAt: OffsetDateTime
)
