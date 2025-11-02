package com.visor.school.platform.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        
        // Skip authentication for actuator endpoints
        if (request.uri.path.contains("/actuator")) {
            return chain.filter(exchange)
        }

        // Extract and validate JWT token
        val authHeader = request.headers.getFirst("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header")
            // For now, allow the request to pass - authentication will be handled by OAuth2 Resource Server
        }

        return chain.filter(exchange)
    }

    override fun getOrder(): Int = -100
}

