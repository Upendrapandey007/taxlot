package com.taxlot.accounting.domain

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class JournalLine(
    val id: UUID = UUID.randomUUID(),
    val journalEntryId: UUID,
    val organizationId: UUID,
    val accountId: UUID,
    val lineNumber: Int,
    val debit: BigDecimal = BigDecimal.ZERO,
    val credit: BigDecimal = BigDecimal.ZERO,
    val currencyCode: String = "USD",
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val description: String? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)