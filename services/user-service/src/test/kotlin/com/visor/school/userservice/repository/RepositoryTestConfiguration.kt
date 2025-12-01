package com.visor.school.userservice.repository

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.mockito.Mockito.mock
import com.visor.school.userservice.event.UserEventPublisher
import org.springframework.security.oauth2.jwt.JwtDecoder

@TestConfiguration
class RepositoryTestConfiguration {
    
    @Bean
    @Primary
    fun rabbitTemplate(): RabbitTemplate {
        return mock(RabbitTemplate::class.java)
    }
    
    @Bean
    @Primary
    fun userEventPublisher(): UserEventPublisher {
        return mock(UserEventPublisher::class.java)
    }
    
    @Bean
    @Primary
    fun jwtDecoder(): JwtDecoder {
        return mock(JwtDecoder::class.java)
    }
}

