package com.visor.school.keycloak.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.visor.school.keycloak")
@EnableConfigurationProperties(InitializerProperties.class)
public class KeycloakInitializerAutoConfiguration {
}
