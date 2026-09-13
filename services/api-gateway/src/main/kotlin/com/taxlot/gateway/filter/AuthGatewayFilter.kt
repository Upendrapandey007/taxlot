package com.taxlot.gateway.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.taxlot.gateway.security.JwtValidator
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthGatewayFilter(
    private val jwtValidator: JwtValidator,
    private val objectMapper: ObjectMapper
) : GatewayFilter, GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (path.startsWith("/api/v1/auth/")) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange)
        }

        val token = authHeader.substring(7)
        return try {
            val claims = jwtValidator.validateAndParse(token)
            val mutatedRequest = exchange.request.mutate()
                .header("X-User-Id", claims.userId.toString())
                .build()
            val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
            chain.filter(mutatedExchange)
        } catch (ex: Exception) {
            unauthorized(exchange)
        }
    }

    private fun unauthorized(exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON
        val bytes = objectMapper.writeValueAsBytes(mapOf("code" to "UNAUTHORIZED", "message" to "Authentication required"))
        val buffer = response.bufferFactory().wrap(bytes)
        return response.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int = -100
}
