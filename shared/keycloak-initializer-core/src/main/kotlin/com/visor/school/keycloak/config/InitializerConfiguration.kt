package com.visor.school.keycloak.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(InitializerProperties::class)
class InitializerConfiguration
