package com.visor.school.userservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("User Service API")
                    .version("1.0.0")
                    .description("User management service with Keycloak OAuth2/JWT authentication")
                    .contact(
                        Contact()
                            .name("School Management System")
                            .email("support@school-management.example.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8081").description("Local development server"),
                    Server().url("https://api.school-management.example.com").description("Production server")
                )
            )
    }
}

