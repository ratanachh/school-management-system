package com.visor.school.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(
    scanBasePackages = ["com.visor.school.userservice", "com.visor.school.keycloak"]
)
@EnableDiscoveryClient
@ComponentScan(
    basePackages = ["com.visor.school.userservice", "com.visor.school.keycloak"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = [".*KeycloakInitializerApplication.*"]
    )]
)
class UserServiceApplication

fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}

