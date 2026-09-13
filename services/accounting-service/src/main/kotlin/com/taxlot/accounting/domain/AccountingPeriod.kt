package com.taxlot.accounting.domain

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class AccountingPeriod(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: PeriodStatus = PeriodStatus.OPEN,
    val lockedAt: OffsetDateTime? = null,
    val lockedBy: UUID? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)