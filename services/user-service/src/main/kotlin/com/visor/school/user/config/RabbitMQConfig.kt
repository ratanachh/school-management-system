package com.visor.school.user.config

import com.visor.school.common.constant.Constants
import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Bean
    fun schoolManagementExchange(): TopicExchange {
        return TopicExchange(Constants.EXCHANGE_SCHOOL_MANAGEMENT, true, false)
    }

    @Bean
    fun userEventsQueue(): Queue {
        return Queue(Constants.QUEUE_USER_EVENTS, true)
    }

    @Bean
    fun userEventsBinding(): Binding {
        return BindingBuilder.bind(userEventsQueue())
            .to(schoolManagementExchange())
            .with("user.*")
    }
}

