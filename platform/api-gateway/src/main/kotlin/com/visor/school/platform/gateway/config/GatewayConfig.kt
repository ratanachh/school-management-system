package com.visor.school.platform.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("user-service") { r ->
                r.path("/api/users/**")
                    .filters { f -> f.stripPrefix(1) }
                    .uri("lb://user-service")
            }
            .route("user-service-health") { r ->
                r.path("/actuator/**")
                    .uri("lb://user-service")
            }
            .build()
    }
}

