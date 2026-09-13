package com.taxlot.accounting.application

import com.taxlot.accounting.domain.AccountType
import com.taxlot.accounting.domain.NormalBalance
import com.taxlot.accounting.domain.exceptions.BusinessRuleException
import com.taxlot.accounting.infrastructure.LedgerBalanceRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

data class TrialBalanceItem(
    val accountId: UUID,
    val code: String,
    val name: String,
    val type: AccountType,
    val debitTotal: BigDecimal,
    val creditTotal: BigDecimal,
    val netBalance: BigDecimal
)

data class TrialBalanceReport(
    val organizationId: UUID,
    val periodId: UUID,
    val items: List<TrialBalanceItem>,
    val totalDebits: BigDecimal,
    val totalCredits: BigDecimal,
    val isBalanced: Boolean
)

@Service
class GetTrialBalanceUseCase(private val ledgerBalanceRepository: LedgerBalanceRepository) {
    fun execute(organizationId: UUID, periodId: UUID): TrialBalanceReport {
        val rawData = ledgerBalanceRepository.getTrialBalance(organizationId, periodId)
        
        val items = rawData.map { row ->
            val type = AccountType.valueOf(row["type"] as String)
            TrialBalanceItem(
                accountId = row["account_id"] as UUID,
                code = row["code"] as String,
                name = row["name"] as String,
                type = type,
                debitTotal = row["debit_total"] as BigDecimal,
                creditTotal = row["credit_total"] as BigDecimal,
                netBalance = row["net_balance"] as BigDecimal
            )
        }
        
        val totalDebits = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.debitTotal) }
        val totalCredits = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.creditTotal) }
        val isBalanced = totalDebits.compareTo(totalCredits) == 0

        if (!isBalanced) {
            throw BusinessRuleException("TRIAL_BALANCE_MISMATCH", "Trial balance is not balanced: Debits $totalDebits != Credits $totalCredits")
        }

        return TrialBalanceReport(organizationId, periodId, items, totalDebits, totalCredits, isBalanced)
    }
}
