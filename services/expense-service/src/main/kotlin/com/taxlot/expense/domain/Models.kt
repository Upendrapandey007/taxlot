package com.taxlot.expense.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class ExpenseStatus { RECORDED, VOID }

enum class ExpenseCategory {
    MEALS, TRAVEL, OFFICE_SUPPLIES, UTILITIES, RENT, ADVERTISING, SOFTWARE, OTHER
}

enum class ExpensePaymentMethod {
    CASH, BANK_TRANSFER, CREDIT_CARD, OTHER
}

data class Expense(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val expenseNumber: String,
    val vendorName: String,
    val category: ExpenseCategory,
    val expenseDate: LocalDate,
    val paymentMethod: ExpensePaymentMethod = ExpensePaymentMethod.BANK_TRANSFER,
    val currencyCode: String = "USD",
    val exchangeRate: BigDecimal = BigDecimal("1.000000"),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val receiptUrl: String? = null,
    val notes: String? = null,
    val status: ExpenseStatus = ExpenseStatus.RECORDED,
    val createdBy: UUID? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    init {
        require(subtotal + taxAmount == totalAmount) {
            "Total amount must be the sum of subtotal and tax amount"
        }
    }
}
