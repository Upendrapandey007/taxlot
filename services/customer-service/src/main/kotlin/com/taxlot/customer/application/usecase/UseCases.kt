package com.taxlot.customer.application.usecase

import com.taxlot.customer.domain.*
import com.taxlot.customer.infrastructure.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CustomerUseCases(
    private val customerRepository: CustomerRepository,
    private val customerAddressRepository: CustomerAddressRepository,
    private val customerContactRepository: CustomerContactRepository
) {
    @Transactional
    fun createCustomer(orgId: UUID, name: String, email: String?, phone: String?, companyName: String?, taxNumber: String?, currencyCode: String?, notes: String?): Customer {
        val customer = Customer(
            organizationId = orgId,
            name = name,
            email = email,
            phone = phone,
            companyName = companyName,
            taxNumber = taxNumber,
            currencyCode = currencyCode ?: "USD",
            notes = notes
        )
        return customerRepository.save(customer)
    }

    @Transactional(readOnly = true)
    fun getCustomer(id: UUID, orgId: UUID): Customer? {
        return customerRepository.findByIdAndOrganizationId(id, orgId)
    }

    @Transactional(readOnly = true)
    fun listCustomers(orgId: UUID, search: String?): List<Customer> {
        return customerRepository.findAllByOrganizationId(orgId, search)
    }

    @Transactional
    fun updateCustomer(id: UUID, orgId: UUID, name: String, email: String?, phone: String?, companyName: String?, taxNumber: String?, currencyCode: String?, notes: String?): Customer? {
        val existing = customerRepository.findByIdAndOrganizationId(id, orgId) ?: return null
        val updated = existing.copy(
            name = name,
            email = email,
            phone = phone,
            companyName = companyName,
            taxNumber = taxNumber,
            currencyCode = currencyCode ?: existing.currencyCode,
            notes = notes,
            updatedAt = java.time.OffsetDateTime.now()
        )
        return customerRepository.save(updated)
    }

    @Transactional
    fun deleteCustomer(id: UUID, orgId: UUID): Boolean {
        val existing = customerRepository.findByIdAndOrganizationId(id, orgId) ?: return false
        val updated = existing.copy(status = CustomerStatus.INACTIVE, updatedAt = java.time.OffsetDateTime.now())
        customerRepository.save(updated)
        return true
    }
}
