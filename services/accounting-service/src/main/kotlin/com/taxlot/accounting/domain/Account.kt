package com.taxlot.accounting.domain

import java.util.UUID
import java.time.OffsetDateTime

data class Account(
    val id: UUID = UUID.randomUUID(),
    val organizationId: UUID,
    val code: String,
    val name: String,
    val type: AccountType,
    val normalBalance: NormalBalance = type.normalBalance,
    val parentId: UUID? = null,
    val isSystem: Boolean = false,
    val isActive: Boolean = true,
    val description: String? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)