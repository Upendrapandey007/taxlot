package com.taxlot.customer.presentation.api

import com.taxlot.customer.application.usecase.CustomerUseCases
import com.taxlot.customer.domain.Customer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateCustomerRequest(
    val name: String,
    val email: String?,
    val phone: String?,
    val companyName: String?,
    val taxNumber: String?,
    val currencyCode: String?,
    val notes: String?
)

data class UpdateCustomerRequest(
    val name: String,
    val email: String?,
    val phone: String?,
    val companyName: String?,
    val taxNumber: String?,
    val currencyCode: String?,
    val notes: String?
)

@RestController
@RequestMapping("/api/v1/customers")
class CustomersController(private val customerUseCases: CustomerUseCases) {

    @PostMapping
    fun createCustomer(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @RequestBody req: CreateCustomerRequest
    ): ResponseEntity<Customer> {
        val orgId = UUID.fromString(orgIdStr)
        val customer = customerUseCases.createCustomer(
            orgId, req.name, req.email, req.phone, req.companyName, req.taxNumber, req.currencyCode, req.notes
        )
        return ResponseEntity.ok(customer)
    }

    @GetMapping("/{id}")
    fun getCustomer(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID
    ): ResponseEntity<Customer> {
        val orgId = UUID.fromString(orgIdStr)
        val customer = customerUseCases.getCustomer(id, orgId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(customer)
    }

    @GetMapping
    fun listCustomers(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<List<Customer>> {
        val orgId = UUID.fromString(orgIdStr)
        return ResponseEntity.ok(customerUseCases.listCustomers(orgId, search))
    }

    @PutMapping("/{id}")
    fun updateCustomer(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID,
        @RequestBody req: UpdateCustomerRequest
    ): ResponseEntity<Customer> {
        val orgId = UUID.fromString(orgIdStr)
        val customer = customerUseCases.updateCustomer(
            id, orgId, req.name, req.email, req.phone, req.companyName, req.taxNumber, req.currencyCode, req.notes
        ) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(customer)
    }

    @DeleteMapping("/{id}")
    fun deleteCustomer(
        @RequestHeader("X-Tenant-Id") orgIdStr: String,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        val orgId = UUID.fromString(orgIdStr)
        if (customerUseCases.deleteCustomer(id, orgId)) {
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.notFound().build()
    }
}
