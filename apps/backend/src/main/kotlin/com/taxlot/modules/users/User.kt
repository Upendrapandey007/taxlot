package com.taxlot.modules.users

import java.time.OffsetDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val emailVerifiedAt: OffsetDateTime? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
