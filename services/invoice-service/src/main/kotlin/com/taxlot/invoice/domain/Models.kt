package com.taxlot.invoice.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

enum class InvoiceStatus { DRAFT, ISSUED, PARTIALLY_PAID, PAID, CANCELLED }

data class Invoice(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val customerId: UUID,
    val invoiceNumber: String,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val issueDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val currencyCode: String = "USD",
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val taxTotal: BigDecimal = BigDecimal.ZERO,
    val discountTotal: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val amountPaid: BigDecimal = BigDecimal.ZERO,
    val outstandingBalance: BigDecimal = BigDecimal.ZERO,
    val notes: String? = null,
    val terms: String? = null,
    val customerSnapshot: String? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    val issuedAt: OffsetDateTime? = null
)

data class InvoiceLine(
    val id: UUID = UUID.randomUUID(),
    val invoiceId: UUID,
    val organizationId: UUID,
    val lineNumber: Int,
    val description: String,
    val quantity: BigDecimal = BigDecimal.ONE,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val taxRate: BigDecimal = BigDecimal.ZERO,
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class InvoiceOutbox(
    val id: UUID = UUID.randomUUID(),
    val eventId: UUID = UUID.randomUUID(),
    val eventType: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val tenantId: UUID?,
    val routingKey: String,
    val payload: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val publishedAt: OffsetDateTime? = null,
    val attemptCount: Int = 0,
    val lastError: String? = null
)
