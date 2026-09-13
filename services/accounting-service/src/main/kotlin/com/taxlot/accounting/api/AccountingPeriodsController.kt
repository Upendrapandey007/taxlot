package com.taxlot.accounting.api

import com.taxlot.accounting.application.ManagePeriodUseCase
import com.taxlot.accounting.domain.AccountingPeriod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounting/periods")
class AccountingPeriodsController(
    private val managePeriodUseCase: ManagePeriodUseCase
) {

    @GetMapping
    fun listPeriods(@RequestHeader("X-Tenant-Id") organizationId: UUID): List<AccountingPeriod> {
        return managePeriodUseCase.listPeriods(organizationId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPeriod(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestBody period: AccountingPeriod
    ): AccountingPeriod {
        return managePeriodUseCase.createPeriod(period.copy(organizationId = organizationId))
    }

    @PostMapping("/{id}/lock")
    @ResponseStatus(HttpStatus.OK)
    fun lockPeriod(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestHeader("X-User-Id", required = false) userId: UUID?,
        @PathVariable id: UUID
    ) {
        managePeriodUseCase.lockPeriod(organizationId, id, userId)
    }
}
