package com.taxlot.accounting.application

import com.taxlot.accounting.domain.Account
import com.taxlot.accounting.infrastructure.AccountRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetChartOfAccountsUseCase(private val accountRepository: AccountRepository) {
    fun execute(organizationId: UUID): List<Account> {
        return accountRepository.listByOrganization(organizationId)
    }
}
