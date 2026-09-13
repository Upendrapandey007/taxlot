package com.taxlot.modules.users.dto

import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val fullName: String,
    val createdAt: OffsetDateTime
)
