package com.taxlot.gateway.filter

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class RateLimitGatewayFilter(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
        val path = exchange.request.path.value()
        val key = "rate_limit:$ip:$path"
        
        val limit = if (path.startsWith("/api/v1/auth")) 10L else 100L

        return redisTemplate.opsForValue().increment(key)
            .flatMap { count ->
                if (count == 1L) {
                    redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(count)
                } else {
                    Mono.just(count)
                }
            }
            .flatMap { count ->
                if (count > limit) {
                    exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                    return@flatMap exchange.response.setComplete()
                } else {
                    return@flatMap chain.filter(exchange)
                }
            }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}
