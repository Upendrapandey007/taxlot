package com.taxlot.gateway.config

import com.taxlot.gateway.filter.AuthGatewayFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRoutesConfig(
    private val properties: GatewayProperties,
    private val authFilter: AuthGatewayFilter
) {
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("auth_service") { r ->
                r.path("/api/v1/auth/**")
                    .filters { f -> f.addRequestHeader("X-Gateway-Source", "api-gateway") }
                    .uri(properties.authServiceUrl)
            }
            .route("user_service") { r ->
                r.path("/api/v1/users/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.userServiceUrl)
            }
            .route("organization_service") { r ->
                r.path("/api/v1/organizations/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.organizationServiceUrl)
            }
            .route("accounting_service") { r ->
                r.path("/api/v1/accounting/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.accountingServiceUrl)
            }
            .route("customer_service") { r ->
                r.path("/api/v1/customers/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.customerServiceUrl)
            }
            .route("invoice_service") { r ->
                r.path("/api/v1/invoices/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.invoiceServiceUrl)
            }
            .route("payment_service") { r ->
                r.path("/api/v1/payments/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.paymentServiceUrl)
            }
            .route("expense_service") { r ->
                r.path("/api/v1/expenses/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri(properties.expenseServiceUrl)
            }
            .route("fallback") { r ->
                r.path("/api/v1/**")
                    .filters { f -> 
                        f.filter(authFilter)
                        f.addRequestHeader("X-Gateway-Source", "api-gateway") 
                    }
                    .uri("no://op") // Adjust as necessary
            }
            .build()
    }
}
