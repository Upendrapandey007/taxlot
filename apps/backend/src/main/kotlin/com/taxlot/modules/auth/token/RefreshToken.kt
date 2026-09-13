package com.taxlot.modules.auth.token

import java.time.OffsetDateTime
import java.util.UUID

data class RefreshToken(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val tokenHash: String,
    val deviceInfo: String? = null,
    val expiresAt: OffsetDateTime,
    val revokedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
