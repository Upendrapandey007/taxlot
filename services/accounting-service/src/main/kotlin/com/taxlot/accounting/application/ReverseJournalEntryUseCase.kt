package com.taxlot.accounting.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.accounting.domain.*
import com.taxlot.accounting.domain.exceptions.ConflictException
import com.taxlot.accounting.domain.exceptions.ValidationException
import com.taxlot.accounting.infrastructure.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class ReverseJournalEntryUseCase(
    private val journalEntryRepository: JournalEntryRepository,
    private val journalLineRepository: JournalLineRepository,
    private val postJournalEntryUseCase: PostJournalEntryUseCase,
    private val outboxRepository: AccountingOutboxRepository,
    private val auditRepository: AccountingAuditRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun execute(organizationId: UUID, entryId: UUID, reversedBy: UUID? = null): JournalEntry {
        val originalEntry = journalEntryRepository.findById(entryId, organizationId)
            ?: throw ValidationException("ENTRY_NOT_FOUND", "Journal entry not found")

        if (originalEntry.status != EntryStatus.POSTED) {
            throw ConflictException("ENTRY_ALREADY_REVERSED", "Journal entry is already reversed")
        }

        val originalLines = journalLineRepository.findByJournalEntryId(entryId)
        
        val reversingLines = originalLines.map { line ->
            JournalLine(
                journalEntryId = UUID.randomUUID(), // Temp
                organizationId = organizationId,
                accountId = line.accountId,
                lineNumber = line.lineNumber,
                debit = line.credit,
                credit = line.debit,
                currencyCode = line.currencyCode,
                exchangeRate = line.exchangeRate,
                description = "Reversal of line ${line.lineNumber}"
            )
        }

        val reversalEntry = postJournalEntryUseCase.execute(
            organizationId = organizationId,
            entryDate = LocalDate.now(),
            description = "Reversal of ${originalEntry.entryNumber}",
            lines = reversingLines,
            reference = "Reversal of ${originalEntry.entryNumber}",
            sourceType = EntrySourceType.REVERSAL,
            createdBy = reversedBy
        )

        journalEntryRepository.updateStatus(originalEntry.id, organizationId, EntryStatus.REVERSED, reversalEntry.id)

        val eventPayload = objectMapper.writeValueAsString(mapOf("originalEntryId" to originalEntry.id, "reversalEntryId" to reversalEntry.id))
        outboxRepository.save(
            OutboxRecord(
                eventType = "JournalEntryReversed",
                aggregateType = "JournalEntry",
                aggregateId = originalEntry.id,
                tenantId = organizationId,
                routingKey = "accounting.journal.reversed",
                payload = eventPayload
            )
        )

        auditRepository.record(organizationId, "REVERSE", "JournalEntry", originalEntry.id, reversedBy, "Reversed by entry ${reversalEntry.entryNumber}")

        return reversalEntry
    }
}
