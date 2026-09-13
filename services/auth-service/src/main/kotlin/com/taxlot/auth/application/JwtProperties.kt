package com.taxlot.auth.application

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "taxlot.jwt")
class JwtProperties {
    lateinit var secret: String
    var accessExpiryMinutes: Long = 15
    var refreshExpiryDays: Long = 30
}
