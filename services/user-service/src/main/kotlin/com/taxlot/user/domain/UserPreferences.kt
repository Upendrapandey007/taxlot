package com.taxlot.user.domain

import java.time.OffsetDateTime
import java.util.UUID

data class UserPreferences(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = false,
    val currencyDisplay: String = "USD",
    val dateFormat: String = "YYYY-MM-DD",
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
