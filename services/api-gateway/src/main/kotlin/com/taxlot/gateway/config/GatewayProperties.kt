package com.taxlot.gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "taxlot.gateway")
class GatewayProperties {
    lateinit var jwtSecret: String
    lateinit var authServiceUrl: String
    lateinit var userServiceUrl: String
    lateinit var organizationServiceUrl: String
    var accountingServiceUrl: String = "http://localhost:8084"
    var customerServiceUrl: String = "http://localhost:8085"
    var invoiceServiceUrl: String = "http://localhost:8086"
    var paymentServiceUrl: String = "http://localhost:8087"
    var expenseServiceUrl: String = "http://localhost:8088"
    var taxServiceUrl: String = "http://localhost:8089"
}
