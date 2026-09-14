package com.taxlot.accounting.infrastructure

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.accounting.application.PostJournalEntryUseCase
import com.taxlot.accounting.domain.EntrySourceType
import com.taxlot.accounting.domain.JournalLine
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Component
class BusinessEventsConsumer(
    private val objectMapper: ObjectMapper,
    private val postJournalEntryUseCase: PostJournalEntryUseCase,
    private val accountRepository: AccountRepository,
    private val journalEntryRepository: JournalEntryRepository
) {
    private val log = LoggerFactory.getLogger(BusinessEventsConsumer::class.java)

    @RabbitListener(queues = ["accounting-service.invoice-issued"])
    fun handleInvoiceIssued(message: String) {
        log.info("Processing invoice.invoice.issued event")
        try {
            val root: JsonNode = objectMapper.readTree(message)
            val payload = if (root.has("payload")) root.get("payload") else root
            
            val organizationId = UUID.fromString(payload.path("organizationId").asText())
            val invoiceId = UUID.fromString(payload.path("invoiceId").asText())
            val invoiceNumber = payload.path("invoiceNumber").asText()
            val totalAmount = BigDecimal(payload.path("totalAmount").asText("0.0000"))
            val subtotal = BigDecimal(payload.path("subtotal").asText(totalAmount.toPlainString()))
            val taxTotal = BigDecimal(payload.path("taxTotal").asText("0.0000"))
            val issueDate = if (payload.hasNonNull("issueDate")) {
                LocalDate.parse(payload.path("issueDate").asText())
            } else {
                LocalDate.now()
            }

            // Accounts: 1100 (AR), 4010 (Sales Revenue), 2100 (Tax Payable)
            val arAccount = accountRepository.findByCode(organizationId, "1100")
            val salesAccount = accountRepository.findByCode(organizationId, "4010")
            val taxAccount = if (taxTotal > BigDecimal.ZERO) accountRepository.findByCode(organizationId, "2100") else null

            if (arAccount == null || salesAccount == null) {
                log.warn("Cannot post invoice journal entry: required accounts (1100 or 4010) not found for org: {}", organizationId)
                return
            }

            val lines = mutableListOf<JournalLine>()
            // Line 1: Debit Accounts Receivable (total amount)
            lines.add(
                JournalLine(
                    organizationId = organizationId,
                    accountId = arAccount.id,
                    lineNumber = 1,
                    debit = totalAmount,
                    credit = BigDecimal.ZERO,
                    description = "Accounts Receivable - Invoice $invoiceNumber"
                )
            )

            // Line 2: Credit Sales Revenue (subtotal)
            lines.add(
                JournalLine(
                    organizationId = organizationId,
                    accountId = salesAccount.id,
                    lineNumber = 2,
                    debit = BigDecimal.ZERO,
                    credit = subtotal,
                    description = "Sales Revenue - Invoice $invoiceNumber"
                )
            )

            // Line 3: Credit Tax Payable (if tax exists)
            if (taxTotal > BigDecimal.ZERO && taxAccount != null) {
                lines.add(
                    JournalLine(
                        organizationId = organizationId,
                        accountId = taxAccount.id,
                        lineNumber = 3,
                        debit = BigDecimal.ZERO,
                        credit = taxTotal,
                        description = "Tax/VAT Payable - Invoice $invoiceNumber"
                    )
                )
            } else if (taxTotal > BigDecimal.ZERO) {
                // If tax account not found, roll tax into sales line so entry remains balanced
                val adjustedLines = lines.toMutableList()
                adjustedLines[1] = adjustedLines[1].copy(credit = totalAmount)
                lines.clear()
                lines.addAll(adjustedLines)
            }

            postJournalEntryUseCase.execute(
                organizationId = organizationId,
                entryDate = issueDate,
                description = "Invoice issued: $invoiceNumber",
                lines = lines,
                reference = invoiceNumber,
                sourceType = EntrySourceType.INVOICE
            )
            log.info("Successfully posted journal entry for invoice {}", invoiceNumber)
        } catch (e: Exception) {
            log.error("Failed to process invoice-issued event", e)
            throw e
        }
    }

    @RabbitListener(queues = ["accounting-service.payment-received"])
    fun handlePaymentReceived(message: String) {
        log.info("Processing payment.payment.received event")
        try {
            val root: JsonNode = objectMapper.readTree(message)
            val payload = if (root.has("payload")) root.get("payload") else root

            val organizationId = UUID.fromString(payload.path("organizationId").asText())
            val paymentId = UUID.fromString(payload.path("paymentId").asText())
            val paymentNumber = payload.path("paymentNumber").asText()
            val amount = BigDecimal(payload.path("amount").asText("0.0000"))
            val paymentMethod = payload.path("paymentMethod").asText("BANK_TRANSFER")
            val paymentDate = if (payload.hasNonNull("paymentDate")) {
                LocalDate.parse(payload.path("paymentDate").asText())
            } else {
                LocalDate.now()
            }

            val bankAccount = accountRepository.findByCode(organizationId, if (paymentMethod == "CASH") "1010" else "1020")
                ?: accountRepository.findByCode(organizationId, "1020")
                ?: accountRepository.findByCode(organizationId, "1010")

            val arAccount = accountRepository.findByCode(organizationId, "1100")

            if (bankAccount == null || arAccount == null) {
                log.warn("Cannot post payment journal entry: required accounts not found for org: {}", organizationId)
                return
            }

            val lines = listOf(
                JournalLine(
                    organizationId = organizationId,
                    accountId = bankAccount.id,
                    lineNumber = 1,
                    debit = amount,
                    credit = BigDecimal.ZERO,
                    description = "Payment received - $paymentNumber"
                ),
                JournalLine(
                    organizationId = organizationId,
                    accountId = arAccount.id,
                    lineNumber = 2,
                    debit = BigDecimal.ZERO,
                    credit = amount,
                    description = "AR cleared by payment - $paymentNumber"
                )
            )

            postJournalEntryUseCase.execute(
                organizationId = organizationId,
                entryDate = paymentDate,
                description = "Payment received: $paymentNumber",
                lines = lines,
                reference = paymentNumber,
                sourceType = EntrySourceType.PAYMENT
            )
            log.info("Successfully posted journal entry for payment {}", paymentNumber)
        } catch (e: Exception) {
            log.error("Failed to process payment-received event", e)
            throw e
        }
    }

    @RabbitListener(queues = ["accounting-service.expense-recorded"])
    fun handleExpenseRecorded(message: String) {
        log.info("Processing expense.expense.recorded event")
        try {
            val root: JsonNode = objectMapper.readTree(message)
            val payload = if (root.has("payload")) root.get("payload") else root

            val organizationId = UUID.fromString(payload.path("organizationId").asText())
            val expenseId = UUID.fromString(payload.path("expenseId").asText())
            val expenseNumber = payload.path("expenseNumber").asText()
            val category = payload.path("category").asText("OTHER")
            val totalAmount = BigDecimal(payload.path("totalAmount").asText("0.0000"))
            val paymentMethod = payload.path("paymentMethod").asText("BANK_TRANSFER")
            val expenseDate = if (payload.hasNonNull("expenseDate")) {
                LocalDate.parse(payload.path("expenseDate").asText())
            } else {
                LocalDate.now()
            }

            // Map category to expense account code
            val expenseAccountCode = when (category) {
                "ADVERTISING" -> "5020"
                "RENT" -> "5030"
                "UTILITIES" -> "5040"
                "OFFICE_SUPPLIES" -> "5060"
                "TRAVEL", "MEALS" -> "5080"
                else -> "5090"
            }

            val expenseAccount = accountRepository.findByCode(organizationId, expenseAccountCode)
                ?: accountRepository.findByCode(organizationId, "5090")

            val cashAccount = accountRepository.findByCode(organizationId, if (paymentMethod == "CASH") "1010" else "1020")
                ?: accountRepository.findByCode(organizationId, "1020")
                ?: accountRepository.findByCode(organizationId, "1010")

            if (expenseAccount == null || cashAccount == null) {
                log.warn("Cannot post expense journal entry: required accounts not found for org: {}", organizationId)
                return
            }

            val lines = listOf(
                JournalLine(
                    organizationId = organizationId,
                    accountId = expenseAccount.id,
                    lineNumber = 1,
                    debit = totalAmount,
                    credit = BigDecimal.ZERO,
                    description = "Expense - $expenseNumber ($category)"
                ),
                JournalLine(
                    organizationId = organizationId,
                    accountId = cashAccount.id,
                    lineNumber = 2,
                    debit = BigDecimal.ZERO,
                    credit = totalAmount,
                    description = "Payment for expense - $expenseNumber"
                )
            )

            postJournalEntryUseCase.execute(
                organizationId = organizationId,
                entryDate = expenseDate,
                description = "Expense recorded: $expenseNumber",
                lines = lines,
                reference = expenseNumber,
                sourceType = EntrySourceType.EXPENSE
            )
            log.info("Successfully posted journal entry for expense {}", expenseNumber)
        } catch (e: Exception) {
            log.error("Failed to process expense-recorded event", e)
            throw e
        }
    }
}
