package com.taxlot.accounting.domain

import com.taxlot.accounting.domain.exceptions.BusinessRuleException
import java.math.BigDecimal

object DoubleEntryValidator {
    fun validate(lines: List<JournalLine>) {
        require(lines.size >= 2) { "Journal entry must contain at least 2 lines" }
        val totalDebits = lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.debit) }
        val totalCredits = lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.credit) }
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw BusinessRuleException("JOURNAL_UNBALANCED", "Total debits ($totalDebits) must equal total credits ($totalCredits)")
        }
        if (totalDebits.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessRuleException("JOURNAL_ZERO_AMOUNT", "Transaction amounts must be greater than zero")
        }
        
        lines.forEach { line ->
            if (line.debit < BigDecimal.ZERO || line.credit < BigDecimal.ZERO) {
                throw BusinessRuleException("JOURNAL_NEGATIVE_AMOUNT", "Amounts cannot be negative")
            }
            if (line.debit > BigDecimal.ZERO && line.credit > BigDecimal.ZERO) {
                throw BusinessRuleException("JOURNAL_INVALID_LINE", "Line cannot have both debit and credit")
            }
        }
    }
}