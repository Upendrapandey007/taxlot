package com.taxlot.invoice.application.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.invoice.domain.*
import com.taxlot.invoice.infrastructure.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class InvoiceUseCases(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceLineRepository: InvoiceLineRepository,
    private val outboxRepository: InvoiceOutboxRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional
    fun createDraftInvoice(orgId: UUID, customerId: UUID, currencyCode: String?, lines: List<InvoiceLineInput>): Invoice {
        val invoiceNumber = invoiceRepository.getNextInvoiceNumber(orgId, LocalDate.now().year)
        var subtotal = BigDecimal.ZERO
        var taxTotal = BigDecimal.ZERO
        
        val invoiceId = UUID.randomUUID()
        val invoiceLines = lines.mapIndexed { index, input ->
            val lineSub = input.quantity * input.unitPrice
            val lineTax = lineSub * input.taxRate
            val lineTot = lineSub + lineTax
            subtotal += lineSub
            taxTotal += lineTax
            InvoiceLine(
                invoiceId = invoiceId,
                organizationId = orgId,
                lineNumber = index + 1,
                description = input.description,
                quantity = input.quantity,
                unitPrice = input.unitPrice,
                taxRate = input.taxRate,
                taxAmount = lineTax,
                subtotal = lineSub,
                totalAmount = lineTot
            )
        }
        
        val totalAmount = subtotal + taxTotal
        
        val invoice = Invoice(
            id = invoiceId,
            organizationId = orgId,
            customerId = customerId,
            invoiceNumber = invoiceNumber,
            status = InvoiceStatus.DRAFT,
            currencyCode = currencyCode ?: "USD",
            subtotal = subtotal,
            taxTotal = taxTotal,
            totalAmount = totalAmount,
            outstandingBalance = totalAmount
        )
        
        invoiceRepository.save(invoice)
        invoiceLineRepository.saveAll(invoiceLines)
        return invoice
    }

    @Transactional
    fun updateDraftInvoice(id: UUID, orgId: UUID, lines: List<InvoiceLineInput>): Invoice? {
        val existing = invoiceRepository.findByIdAndOrganizationId(id, orgId) ?: return null
        if (existing.status != InvoiceStatus.DRAFT) throw IllegalStateException("Only DRAFT invoices can be updated")
        
        invoiceLineRepository.deleteByInvoiceId(id)
        
        var subtotal = BigDecimal.ZERO
        var taxTotal = BigDecimal.ZERO
        
        val invoiceLines = lines.mapIndexed { index, input ->
            val lineSub = input.quantity * input.unitPrice
            val lineTax = lineSub * input.taxRate
            val lineTot = lineSub + lineTax
            subtotal += lineSub
            taxTotal += lineTax
            InvoiceLine(
                invoiceId = id,
                organizationId = orgId,
                lineNumber = index + 1,
                description = input.description,
                quantity = input.quantity,
                unitPrice = input.unitPrice,
                taxRate = input.taxRate,
                taxAmount = lineTax,
                subtotal = lineSub,
                totalAmount = lineTot
            )
        }
        
        invoiceLineRepository.saveAll(invoiceLines)
        
        val totalAmount = subtotal + taxTotal
        val updated = existing.copy(
            subtotal = subtotal,
            taxTotal = taxTotal,
            totalAmount = totalAmount,
            outstandingBalance = totalAmount,
            updatedAt = OffsetDateTime.now()
        )
        
        return invoiceRepository.save(updated)
    }

    @Transactional
    fun issueInvoice(id: UUID, orgId: UUID): Invoice? {
        val existing = invoiceRepository.findByIdAndOrganizationId(id, orgId) ?: return null
        if (existing.status != InvoiceStatus.DRAFT) throw IllegalStateException("Only DRAFT invoices can be issued")
        
        val updated = existing.copy(
            status = InvoiceStatus.ISSUED,
            issuedAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
        invoiceRepository.save(updated)
        
        val payload = mapOf("invoiceId" to updated.id.toString(), "amount" to updated.totalAmount.toString())
        val outbox = InvoiceOutbox(
            eventType = "InvoiceIssued",
            aggregateType = "Invoice",
            aggregateId = updated.id,
            tenantId = orgId,
            routingKey = "invoice.invoice.issued",
            payload = objectMapper.writeValueAsString(payload)
        )
        outboxRepository.save(outbox)
        return updated
    }

    @Transactional
    fun cancelInvoice(id: UUID, orgId: UUID): Invoice? {
        val existing = invoiceRepository.findByIdAndOrganizationId(id, orgId) ?: return null
        val updated = existing.copy(
            status = InvoiceStatus.CANCELLED,
            updatedAt = OffsetDateTime.now()
        )
        invoiceRepository.save(updated)
        
        val payload = mapOf("invoiceId" to updated.id.toString())
        val outbox = InvoiceOutbox(
            eventType = "InvoiceCancelled",
            aggregateType = "Invoice",
            aggregateId = updated.id,
            tenantId = orgId,
            routingKey = "invoice.invoice.cancelled",
            payload = objectMapper.writeValueAsString(payload)
        )
        outboxRepository.save(outbox)
        return updated
    }

    @Transactional(readOnly = true)
    fun getInvoice(id: UUID, orgId: UUID): Invoice? {
        return invoiceRepository.findByIdAndOrganizationId(id, orgId)
    }

    @Transactional(readOnly = true)
    fun listInvoices(orgId: UUID): List<Invoice> {
        return invoiceRepository.findAllByOrganizationId(orgId)
    }
    
    @Transactional
    fun applyPayment(id: UUID, orgId: UUID, amount: BigDecimal) {
        val invoice = invoiceRepository.findByIdAndOrganizationId(id, orgId) ?: return
        val newPaid = invoice.amountPaid + amount
        val newBalance = invoice.totalAmount - newPaid
        val newStatus = if (newBalance <= BigDecimal.ZERO) InvoiceStatus.PAID else InvoiceStatus.PARTIALLY_PAID
        
        val updated = invoice.copy(
            amountPaid = newPaid,
            outstandingBalance = newBalance,
            status = newStatus,
            updatedAt = OffsetDateTime.now()
        )
        invoiceRepository.save(updated)
    }
}

data class InvoiceLineInput(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val taxRate: BigDecimal
)
