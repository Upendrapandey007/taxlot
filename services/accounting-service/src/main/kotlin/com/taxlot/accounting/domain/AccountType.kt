package com.taxlot.accounting.domain

enum class AccountType(val normalBalance: NormalBalance) {
    ASSET(NormalBalance.DEBIT),
    LIABILITY(NormalBalance.CREDIT),
    EQUITY(NormalBalance.CREDIT),
    REVENUE(NormalBalance.CREDIT),
    EXPENSE(NormalBalance.DEBIT)
}