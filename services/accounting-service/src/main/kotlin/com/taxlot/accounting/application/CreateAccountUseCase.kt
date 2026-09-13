package com.taxlot.accounting.application

import com.taxlot.accounting.domain.Account
import com.taxlot.accounting.domain.exceptions.ConflictException
import com.taxlot.accounting.infrastructure.AccountRepository
import org.springframework.stereotype.Service

@Service
class CreateAccountUseCase(private val accountRepository: AccountRepository) {
    fun execute(account: Account): Account {
        if (accountRepository.existsByCode(account.organizationId, account.code)) {
            throw ConflictException("ACCOUNT_CODE_EXISTS", "Account with code ${account.code} already exists")
        }
        return accountRepository.save(account)
    }
}
