package com.taxlot.accounting.domain

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class JournalEntry(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val periodId: UUID,
    val entryNumber: String,
    val entryDate: LocalDate,
    val reference: String? = null,
    val description: String? = null,
    val sourceType: EntrySourceType = EntrySourceType.MANUAL,
    val sourceId: UUID? = null,
    val status: EntryStatus = EntryStatus.POSTED,
    val reversedByEntryId: UUID? = null,
    val createdBy: UUID? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val postedAt: OffsetDateTime = OffsetDateTime.now(),
    val lines: List<JournalLine> = emptyList()
)