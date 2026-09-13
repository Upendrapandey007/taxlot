package com.taxlot.accounting.application

import com.taxlot.accounting.domain.Account
import com.taxlot.accounting.domain.AccountType
import com.taxlot.accounting.domain.AccountingPeriod
import com.taxlot.accounting.domain.NormalBalance
import com.taxlot.accounting.domain.PeriodStatus
import com.taxlot.accounting.infrastructure.AccountRepository
import com.taxlot.accounting.infrastructure.AccountingPeriodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class InitializeDefaultAccountsUseCase(
    private val accountRepository: AccountRepository,
    private val accountingPeriodRepository: AccountingPeriodRepository
) {
    @Transactional
    fun execute(organizationId: UUID) {
        val accounts = listOf(
            Account(organizationId = organizationId, code = "1010", name = "Cash", type = AccountType.ASSET, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "1020", name = "Bank", type = AccountType.ASSET, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "1100", name = "Accounts Receivable", type = AccountType.ASSET, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "1200", name = "Inventory", type = AccountType.ASSET, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "2010", name = "Accounts Payable", type = AccountType.LIABILITY, normalBalance = NormalBalance.CREDIT, isSystem = true),
            Account(organizationId = organizationId, code = "2100", name = "VAT Payable", type = AccountType.LIABILITY, normalBalance = NormalBalance.CREDIT, isSystem = true),
            Account(organizationId = organizationId, code = "3010", name = "Owner's Equity", type = AccountType.EQUITY, normalBalance = NormalBalance.CREDIT, isSystem = true),
            Account(organizationId = organizationId, code = "3020", name = "Retained Earnings", type = AccountType.EQUITY, normalBalance = NormalBalance.CREDIT, isSystem = true),
            Account(organizationId = organizationId, code = "4010", name = "Sales Revenue", type = AccountType.REVENUE, normalBalance = NormalBalance.CREDIT, isSystem = true),
            Account(organizationId = organizationId, code = "5010", name = "Cost of Goods Sold", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "5020", name = "Advertising", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "5030", name = "Rent", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "5040", name = "Utilities", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "5050", name = "Salaries", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true),
            Account(organizationId = organizationId, code = "5060", name = "Office Supplies", type = AccountType.EXPENSE, normalBalance = NormalBalance.DEBIT, isSystem = true)
        )
        accountRepository.saveAll(accounts)

        val currentYear = LocalDate.now().year
        val start = LocalDate.of(currentYear, 1, 1)
        val end = LocalDate.of(currentYear, 12, 31)
        
        val existingPeriod = accountingPeriodRepository.findPeriodForDate(organizationId, LocalDate.now())
        if (existingPeriod == null) {
            val period = AccountingPeriod(
                organizationId = organizationId,
                name = "Current Year $currentYear",
                startDate = start,
                endDate = end,
                status = PeriodStatus.OPEN
            )
            accountingPeriodRepository.save(period)
        }
    }
}
