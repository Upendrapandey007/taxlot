package com.taxlot.user.domain

import java.time.OffsetDateTime
import java.util.UUID

data class UserProfile(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val locale: String = "en",
    val timezone: String = "UTC",
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
)
