package com.taxlot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "taxlot.jwt")
data class JwtProperties(
    val secret: String,
    val accessExpiryMinutes: Long,
    val refreshExpiryDays: Long
)
