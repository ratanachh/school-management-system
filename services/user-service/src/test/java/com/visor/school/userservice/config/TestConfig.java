package com.visor.school.userservice.config;

import com.visor.school.userservice.integration.KeycloakClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.mockito.Mockito.mock;

/**
 * Test-only beans when external infra (RabbitMQ, OAuth2, Keycloak) is disabled.
 * Active when profile "test" is used; provides mocks so the application context can start.
 */
@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    public KeycloakClient keycloakClient() {
        return mock(KeycloakClient.class);
    }

    /**
     * Test security chain: no OAuth2/JWT so @WithMockUser is used.
     * Takes precedence over main SecurityConfig so JWT filter does not run.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher(new AntPathRequestMatcher("/**"))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/actuator/**"),
                    new AntPathRequestMatcher("/v1/auth/**"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"))
                .permitAll()
                .anyRequest().authenticated())
            .build();
    }
}
