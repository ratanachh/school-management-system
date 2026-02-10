package com.visor.school.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
    scanBasePackages = {"com.visor.school.userservice", "com.visor.school.keycloak"}
)
@EnableDiscoveryClient
@ComponentScan(
    basePackages = {"com.visor.school.userservice", "com.visor.school.keycloak"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = ".*KeycloakInitializerApplication.*"
    )
)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
