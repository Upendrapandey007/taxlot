package com.taxlot.accounting.application

import com.taxlot.accounting.domain.AccountingPeriod
import com.taxlot.accounting.domain.PeriodStatus
import com.taxlot.accounting.domain.exceptions.ValidationException
import com.taxlot.accounting.infrastructure.AccountingPeriodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ManagePeriodUseCase(
    private val accountingPeriodRepository: AccountingPeriodRepository
) {
    @Transactional
    fun createPeriod(period: AccountingPeriod): AccountingPeriod {
        return accountingPeriodRepository.save(period)
    }

    fun listPeriods(organizationId: UUID): List<AccountingPeriod> {
        return accountingPeriodRepository.listByOrganization(organizationId)
    }

    @Transactional
    fun lockPeriod(organizationId: UUID, periodId: UUID, lockedBy: UUID?) {
        val period = accountingPeriodRepository.findById(periodId, organizationId)
            ?: throw ValidationException("PERIOD_NOT_FOUND", "Accounting period not found")

        accountingPeriodRepository.updateStatus(periodId, organizationId, PeriodStatus.LOCKED, lockedBy)
    }
}
