package com.taxlot.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("refresh_tokens")
data class RefreshToken(
    @Id val id: UUID? = null,
    val credentialId: UUID,
    val tokenHash: String,
    val deviceInfo: String? = null,
    val ipAddress: String? = null,
    val expiresAt: Instant,
    val revokedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)
