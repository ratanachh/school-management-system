package com.visor.school.userservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * RabbitMQ configuration for user-service.
 * Configures JSON message converter for publishing domain events.
 * Excluded in "test" profile (tests use TestConfig with mock RabbitTemplate).
 */
@Configuration
@Profile("!test")
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "school-management.exchange";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean
    public TopicExchange schoolManagementExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }
}
