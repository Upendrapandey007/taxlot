package com.taxlot.gateway.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class RequestIdGatewayFilter : GlobalFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val reqIdHeader = exchange.request.headers.getFirst("X-Request-Id")
        val requestId = reqIdHeader ?: UUID.randomUUID().toString()

        val request = exchange.request.mutate()
            .header("X-Request-Id", requestId)
            .build()
        
        exchange.response.headers.add("X-Request-Id", requestId)

        return chain.filter(exchange.mutate().request(request).build())
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
