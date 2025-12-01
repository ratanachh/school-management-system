package com.visor.school.userservice.contract

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import com.visor.school.userservice.bootstrap.KeycloakInitializerRunner
import com.visor.school.userservice.bootstrap.DefaultAdminUserInitializer
import com.visor.school.userservice.service.SecurityContextService
import com.visor.school.userservice.config.CustomPermissionEvaluator
import org.mockito.Mockito.mock

@TestConfiguration
class TestConfiguration {
    
    @Bean
    @Primary
    fun keycloakInitializerRunner(): KeycloakInitializerRunner {
        return mock(KeycloakInitializerRunner::class.java)
    }
    
    @Bean
    @Primary
    fun defaultAdminUserInitializer(): DefaultAdminUserInitializer {
        return mock(DefaultAdminUserInitializer::class.java)
    }
    
    @Bean
    @Primary
    fun securityContextService(): SecurityContextService {
        return mock(SecurityContextService::class.java)
    }
    
    @Bean
    @Primary
    fun customPermissionEvaluator(): CustomPermissionEvaluator {
        return mock(CustomPermissionEvaluator::class.java)
    }
}

