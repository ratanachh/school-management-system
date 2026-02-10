package com.visor.school.userservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("User Service API")
                    .version("1.0.0")
                    .description("User management service with Keycloak OAuth2/JWT authentication")
                    .contact(
                        new Contact()
                            .name("School Management System")
                            .email("support@school-management.example.com")
                    )
                    .license(
                        new License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .servers(
                List.of(
                    new Server().url("http://localhost:8081").description("Local development server"),
                    new Server().url("https://api.school-management.example.com").description("Production server")
                )
            );
    }
}
