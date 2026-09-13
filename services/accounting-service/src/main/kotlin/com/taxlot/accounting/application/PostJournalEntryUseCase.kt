package com.taxlot.accounting.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.accounting.domain.*
import com.taxlot.accounting.domain.exceptions.BusinessRuleException
import com.taxlot.accounting.domain.exceptions.ValidationException
import com.taxlot.accounting.infrastructure.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class PostJournalEntryUseCase(
    private val journalEntryRepository: JournalEntryRepository,
    private val journalLineRepository: JournalLineRepository,
    private val accountingPeriodRepository: AccountingPeriodRepository,
    private val accountRepository: AccountRepository,
    private val ledgerBalanceRepository: LedgerBalanceRepository,
    private val outboxRepository: AccountingOutboxRepository,
    private val auditRepository: AccountingAuditRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun execute(organizationId: UUID, entryDate: LocalDate, description: String?, lines: List<JournalLine>, reference: String? = null, sourceType: EntrySourceType = EntrySourceType.MANUAL, createdBy: UUID? = null): JournalEntry {
        val period = accountingPeriodRepository.findPeriodForDate(organizationId, entryDate)
            ?: throw ValidationException("NO_OPEN_PERIOD", "No accounting period found for date")

        if (period.status == PeriodStatus.LOCKED) {
            throw BusinessRuleException("ACCOUNTING_PERIOD_LOCKED", "Period is locked")
        }

        val accountIds = lines.map { it.accountId }.toSet()
        val accounts = accountIds.mapNotNull { accountRepository.findById(it, organizationId) }.associateBy { it.id }
        
        if (accounts.size != accountIds.size) {
            throw ValidationException("INVALID_ACCOUNT", "One or more accounts do not exist or belong to another organization")
        }

        DoubleEntryValidator.validate(lines)

        val entryNumber = journalEntryRepository.getNextEntryNumber(organizationId, entryDate.year)
        val entry = JournalEntry(
            organizationId = organizationId,
            periodId = period.id,
            entryNumber = entryNumber,
            entryDate = entryDate,
            reference = reference,
            description = description,
            sourceType = sourceType,
            createdBy = createdBy,
            lines = lines
        )

        val savedEntry = journalEntryRepository.save(entry)
        
        val updatedLines = lines.map { it.copy(journalEntryId = savedEntry.id) }
        journalLineRepository.saveLines(updatedLines)

        updatedLines.forEach { line ->
            ledgerBalanceRepository.updateBalance(
                organizationId = organizationId,
                accountId = line.accountId,
                periodId = period.id,
                debit = line.debit,
                credit = line.credit
            )
        }

        val eventPayload = objectMapper.writeValueAsString(mapOf("journalEntryId" to savedEntry.id, "entryNumber" to savedEntry.entryNumber))
        outboxRepository.save(
            OutboxRecord(
                eventType = "JournalEntryPosted",
                aggregateType = "JournalEntry",
                aggregateId = savedEntry.id,
                tenantId = organizationId,
                routingKey = "accounting.journal.posted",
                payload = eventPayload
            )
        )

        auditRepository.record(organizationId, "POST", "JournalEntry", savedEntry.id, createdBy, "Posted journal entry $entryNumber")

        return savedEntry.copy(lines = updatedLines)
    }
}
