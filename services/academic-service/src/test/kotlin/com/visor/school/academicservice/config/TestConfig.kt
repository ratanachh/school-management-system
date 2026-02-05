package com.visor.school.academicservice.config

import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.mockito.Mockito

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun mockConnectionFactory(): ConnectionFactory {
        return Mockito.mock(ConnectionFactory::class.java)
    }

    @Bean
    @Primary
    fun mockRabbitTemplate(): RabbitTemplate {
        return Mockito.mock(RabbitTemplate::class.java)
    }
}
