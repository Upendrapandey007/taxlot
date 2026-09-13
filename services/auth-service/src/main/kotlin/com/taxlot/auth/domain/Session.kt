package com.taxlot.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("sessions")
data class Session(
    @Id val id: UUID? = null,
    val credentialId: UUID,
    val userAgent: String? = null,
    val ipAddress: String? = null,
    val lastActiveAt: Instant = Instant.now(),
    val revokedAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)
