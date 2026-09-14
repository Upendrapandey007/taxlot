package com.taxlot.customer.domain

import java.time.OffsetDateTime
import java.util.UUID

enum class CustomerStatus { ACTIVE, INACTIVE }
enum class AddressType { BILLING, SHIPPING }

data class Customer(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val companyName: String? = null,
    val taxNumber: String? = null,
    val currencyCode: String = "USD",
    val status: CustomerStatus = CustomerStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)

data class CustomerAddress(
    val id: UUID = UUID.randomUUID(),
    val customerId: UUID,
    val organizationId: UUID,
    val type: AddressType = AddressType.BILLING,
    val line1: String,
    val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val countryCode: String = "US",
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

data class CustomerContact(
    val id: UUID = UUID.randomUUID(),
    val customerId: UUID,
    val organizationId: UUID,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val isPrimary: Boolean = false,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
