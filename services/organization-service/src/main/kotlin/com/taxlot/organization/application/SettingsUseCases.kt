package com.taxlot.organization.application

import com.taxlot.platform.error.NotFoundException
import com.taxlot.organization.infrastructure.SettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetSettingsUseCase(
    private val settingsRepo: SettingsRepository,
    private val permissionChecker: PermissionChecker
) {
    fun execute(userId: UUID, orgId: UUID): OrganizationSettingsResponse {
        permissionChecker.requirePermission(userId, orgId, "organization.read")
        val settings = settingsRepo.findByOrgId(orgId)
            ?: throw NotFoundException("SETTINGS_NOT_FOUND", "Settings not found")
            
        return OrganizationSettingsResponse(
            id = settings.id,
            organizationId = settings.organizationId,
            fiscalYearStart = settings.fiscalYearStart,
            invoicePrefix = settings.invoicePrefix,
            invoiceNextNumber = settings.invoiceNextNumber,
            expenseApproval = settings.expenseApproval,
            timezone = settings.timezone
        )
    }
}

@Service
class UpdateSettingsUseCase(
    private val settingsRepo: SettingsRepository,
    private val permissionChecker: PermissionChecker
) {
    @Transactional
    fun execute(userId: UUID, orgId: UUID, req: UpdateSettingsRequest): OrganizationSettingsResponse {
        permissionChecker.requirePermission(userId, orgId, "organization.update")
        val settings = settingsRepo.findByOrgId(orgId)
            ?: throw NotFoundException("SETTINGS_NOT_FOUND", "Settings not found")
            
        val updated = settings.copy(
            fiscalYearStart = req.fiscalYearStart ?: settings.fiscalYearStart,
            invoicePrefix = req.invoicePrefix ?: settings.invoicePrefix,
            invoiceNextNumber = req.invoiceNextNumber ?: settings.invoiceNextNumber,
            expenseApproval = req.expenseApproval ?: settings.expenseApproval,
            timezone = req.timezone ?: settings.timezone
        )
        val saved = settingsRepo.update(updated)
        
        return OrganizationSettingsResponse(
            id = saved.id,
            organizationId = saved.organizationId,
            fiscalYearStart = saved.fiscalYearStart,
            invoicePrefix = saved.invoicePrefix,
            invoiceNextNumber = saved.invoiceNextNumber,
            expenseApproval = saved.expenseApproval,
            timezone = saved.timezone
        )
    }
}

data class UpdateSettingsRequest(
    val fiscalYearStart: Int?,
    val invoicePrefix: String?,
    val invoiceNextNumber: Int?,
    val expenseApproval: Boolean?,
    val timezone: String?
)

data class OrganizationSettingsResponse(
    val id: UUID,
    val organizationId: UUID,
    val fiscalYearStart: Int,
    val invoicePrefix: String,
    val invoiceNextNumber: Int,
    val expenseApproval: Boolean,
    val timezone: String
)
