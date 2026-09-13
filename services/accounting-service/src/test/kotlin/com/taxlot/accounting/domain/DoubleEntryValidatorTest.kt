package com.taxlot.accounting.domain

import com.taxlot.accounting.domain.exceptions.BusinessRuleException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID

class DoubleEntryValidatorTest {

    private val orgId = UUID.randomUUID()
    private val entryId = UUID.randomUUID()
    private val acc1 = UUID.randomUUID()
    private val acc2 = UUID.randomUUID()

    @Test
    fun `balanced lines succeed`() {
        val lines = listOf(
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc1, lineNumber = 1, debit = BigDecimal("100.00")),
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc2, lineNumber = 2, credit = BigDecimal("100.00"))
        )
        DoubleEntryValidator.validate(lines) // Should not throw
    }

    @Test
    fun `unbalanced lines throw exception`() {
        val lines = listOf(
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc1, lineNumber = 1, debit = BigDecimal("100.00")),
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc2, lineNumber = 2, credit = BigDecimal("90.00"))
        )
        assertThrows<BusinessRuleException> {
            DoubleEntryValidator.validate(lines)
        }.also {
            assert(it.code == "JOURNAL_UNBALANCED")
        }
    }

    @Test
    fun `zero amount throws exception`() {
        val lines = listOf(
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc1, lineNumber = 1, debit = BigDecimal("0.00")),
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc2, lineNumber = 2, credit = BigDecimal("0.00"))
        )
        assertThrows<BusinessRuleException> {
            DoubleEntryValidator.validate(lines)
        }.also {
            assert(it.code == "JOURNAL_ZERO_AMOUNT")
        }
    }

    @Test
    fun `negative amount throws exception`() {
        val lines = listOf(
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc1, lineNumber = 1, debit = BigDecimal("-100.00")),
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc2, lineNumber = 2, credit = BigDecimal("-100.00"))
        )
        assertThrows<BusinessRuleException> {
            DoubleEntryValidator.validate(lines)
        }.also {
            assert(it.code == "JOURNAL_NEGATIVE_AMOUNT" || it.code == "JOURNAL_UNBALANCED") // Validator checks balance first
        }
    }

    @Test
    fun `less than 2 lines throws exception`() {
        val lines = listOf(
            JournalLine(journalEntryId = entryId, organizationId = orgId, accountId = acc1, lineNumber = 1, debit = BigDecimal("100.00"))
        )
        assertThrows<IllegalArgumentException> {
            DoubleEntryValidator.validate(lines)
        }
    }
}
