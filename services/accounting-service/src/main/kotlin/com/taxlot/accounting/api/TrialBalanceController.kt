package com.taxlot.accounting.api

import com.taxlot.accounting.application.GetTrialBalanceUseCase
import com.taxlot.accounting.application.TrialBalanceReport
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounting/trial-balance")
class TrialBalanceController(
    private val getTrialBalanceUseCase: GetTrialBalanceUseCase
) {

    @GetMapping
    fun getTrialBalance(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam periodId: UUID
    ): TrialBalanceReport {
        return getTrialBalanceUseCase.execute(organizationId, periodId)
    }
}
