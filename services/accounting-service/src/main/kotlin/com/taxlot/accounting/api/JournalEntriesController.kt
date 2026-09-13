package com.taxlot.accounting.api

import com.taxlot.accounting.application.PostJournalEntryUseCase
import com.taxlot.accounting.application.ReverseJournalEntryUseCase
import com.taxlot.accounting.domain.EntrySourceType
import com.taxlot.accounting.domain.JournalEntry
import com.taxlot.accounting.domain.JournalLine
import com.taxlot.accounting.infrastructure.JournalEntryRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

data class PostJournalEntryRequest(
    val entryDate: LocalDate,
    val description: String?,
    val reference: String?,
    val sourceType: EntrySourceType = EntrySourceType.MANUAL,
    val lines: List<JournalLine>
)

data class PageResponse<T>(val content: List<T>, val totalElements: Long)

@RestController
@RequestMapping("/api/v1/accounting/journal-entries")
class JournalEntriesController(
    private val postJournalEntryUseCase: PostJournalEntryUseCase,
    private val reverseJournalEntryUseCase: ReverseJournalEntryUseCase,
    private val journalEntryRepository: JournalEntryRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEntry(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestHeader("X-User-Id", required = false) userId: UUID?,
        @RequestBody request: PostJournalEntryRequest
    ): JournalEntry {
        val lines = request.lines.map { it.copy(organizationId = organizationId) }
        return postJournalEntryUseCase.execute(
            organizationId = organizationId,
            entryDate = request.entryDate,
            description = request.description,
            lines = lines,
            reference = request.reference,
            sourceType = request.sourceType,
            createdBy = userId
        )
    }

    @GetMapping
    fun listEntries(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponse<JournalEntry> {
        val entries = journalEntryRepository.listByOrganization(organizationId, page, size)
        val total = journalEntryRepository.countByOrganization(organizationId)
        return PageResponse(entries, total)
    }

    @GetMapping("/{id}")
    fun getEntry(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @PathVariable id: UUID
    ): JournalEntry {
        return journalEntryRepository.findById(id, organizationId) ?: throw RuntimeException("Not found")
    }

    @PostMapping("/{id}/reverse")
    fun reverseEntry(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestHeader("X-User-Id", required = false) userId: UUID?,
        @PathVariable id: UUID
    ): JournalEntry {
        return reverseJournalEntryUseCase.execute(organizationId, id, userId)
    }
}
