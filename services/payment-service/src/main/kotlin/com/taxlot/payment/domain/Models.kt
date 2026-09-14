package com.taxlot.payment.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class PaymentStatus { COMPLETED, REFUNDED }
enum class PaymentMethodType { CASH, BANK_TRANSFER, CREDIT_CARD, CHEQUE, OTHER }

data class Payment(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val paymentNumber: String,
    val customerId: UUID,
    val amount: BigDecimal,
    val currencyCode: String = "USD",
    val paymentDate: LocalDate,
    val paymentMethod: PaymentMethodType = PaymentMethodType.BANK_TRANSFER,
    val reference: String? = null,
    val notes: String? = null,
    val status: PaymentStatus = PaymentStatus.COMPLETED,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)

data class PaymentAllocation(
    val id: UUID = UUID.randomUUID(),
    val paymentId: UUID,
    val organizationId: UUID,
    val invoiceId: UUID,
    val allocatedAmount: BigDecimal,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
