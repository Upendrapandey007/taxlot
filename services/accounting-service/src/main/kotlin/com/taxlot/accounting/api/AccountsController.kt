package com.taxlot.accounting.api

import com.taxlot.accounting.application.CreateAccountUseCase
import com.taxlot.accounting.application.GetChartOfAccountsUseCase
import com.taxlot.accounting.application.InitializeDefaultAccountsUseCase
import com.taxlot.accounting.domain.Account
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounting/accounts")
class AccountsController(
    private val createAccountUseCase: CreateAccountUseCase,
    private val getChartOfAccountsUseCase: GetChartOfAccountsUseCase,
    private val initializeDefaultAccountsUseCase: InitializeDefaultAccountsUseCase
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestBody request: Account
    ): Account {
        return createAccountUseCase.execute(request.copy(organizationId = organizationId))
    }

    @GetMapping
    fun listAccounts(@RequestHeader("X-Tenant-Id") organizationId: UUID): List<Account> {
        return getChartOfAccountsUseCase.execute(organizationId)
    }

    @PostMapping("/initialize-defaults")
    @ResponseStatus(HttpStatus.OK)
    fun initializeDefaults(@RequestHeader("X-Tenant-Id") organizationId: UUID) {
        initializeDefaultAccountsUseCase.execute(organizationId)
    }
}
