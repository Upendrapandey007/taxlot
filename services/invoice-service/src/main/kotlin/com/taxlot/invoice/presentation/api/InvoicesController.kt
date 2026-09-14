package com.taxlot.invoice.presentation.api

import com.taxlot.invoice.application.usecase.InvoiceUseCases
import com.taxlot.invoice.application.usecase.InvoiceLineInput
import com.taxlot.invoice.domain.Invoice
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateInvoiceRequest(
    val customerId: UUID,
    val currencyCode: String?,
    val lines: List<InvoiceLineInput>
)

data class UpdateInvoiceRequest(
    val lines: List<InvoiceLineInput>
)

@RestController
@RequestMapping("/api/v1/invoices")
class InvoicesController(private val invoiceUseCases: InvoiceUseCases) {

    @PostMapping
    fun createDraftInvoice(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @RequestBody req: CreateInvoiceRequest
    ): ResponseEntity<Invoice> {
        val orgId = UUID.fromString(orgIdStr)
        val invoice = invoiceUseCases.createDraftInvoice(orgId, req.customerId, req.currencyCode, req.lines)
        return ResponseEntity.ok(invoice)
    }
    
    @PutMapping("/{id}")
    fun updateDraftInvoice(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID,
        @RequestBody req: UpdateInvoiceRequest
    ): ResponseEntity<Invoice> {
        val orgId = UUID.fromString(orgIdStr)
        val invoice = invoiceUseCases.updateDraftInvoice(id, orgId, req.lines) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(invoice)
    }
    
    @PostMapping("/{id}/issue")
    fun issueInvoice(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID
    ): ResponseEntity<Invoice> {
        val orgId = UUID.fromString(orgIdStr)
        val invoice = invoiceUseCases.issueInvoice(id, orgId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(invoice)
    }
    
    @PostMapping("/{id}/cancel")
    fun cancelInvoice(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID
    ): ResponseEntity<Invoice> {
        val orgId = UUID.fromString(orgIdStr)
        val invoice = invoiceUseCases.cancelInvoice(id, orgId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(invoice)
    }

    @GetMapping("/{id}")
    fun getInvoice(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID
    ): ResponseEntity<Invoice> {
        val orgId = UUID.fromString(orgIdStr)
        val invoice = invoiceUseCases.getInvoice(id, orgId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(invoice)
    }

    @GetMapping
    fun listInvoices(
        @RequestHeader("X-Tenant-Id") orgIdStr: String
    ): ResponseEntity<List<Invoice>> {
        val orgId = UUID.fromString(orgIdStr)
        return ResponseEntity.ok(invoiceUseCases.listInvoices(orgId))
    }
}
