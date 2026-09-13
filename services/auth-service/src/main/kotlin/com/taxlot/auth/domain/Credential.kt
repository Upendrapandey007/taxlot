package com.taxlot.auth.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("credentials")
data class Credential(
    @Id val id: UUID? = null,
    val userId: UUID,
    val email: String,
    val passwordHash: String,
    val status: CredentialStatus = CredentialStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
