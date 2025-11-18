package com.visor.school.notification.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * RabbitMQ configuration for notification service
 * Auto-declares queues and bindings for event consumption
 */
@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE_NAME = "school-management.exchange"
        
        // Queue names
        const val GRADE_RECORDED_QUEUE = "grade_recorded_queue"
        const val ATTENDANCE_MARKED_QUEUE = "attendance_marked_queue"
        const val ATTENDANCE_SESSION_APPROVED_QUEUE = "attendance_session_approved_queue"
        const val USER_CREATED_QUEUE = "user_created_queue"
        
        // Routing keys
        const val GRADE_RECORDED_ROUTING_KEY = "grade.recorded"
        const val ATTENDANCE_MARKED_ROUTING_KEY = "attendance.marked"
        const val ATTENDANCE_SESSION_APPROVED_ROUTING_KEY = "attendance.session.approved"
        const val USER_CREATED_ROUTING_KEY = "user.created"
    }

    /**
     * Declare the topic exchange
     */
    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(EXCHANGE_NAME, true, false)
    }

    /**
     * Declare grade_recorded_queue
     */
    @Bean
    fun gradeRecordedQueue(): Queue {
        return Queue(GRADE_RECORDED_QUEUE, true, false, false)
    }

    /**
     * Bind grade_recorded_queue to exchange
     */
    @Bean
    fun gradeRecordedBinding(): Binding {
        return BindingBuilder
            .bind(gradeRecordedQueue())
            .to(exchange())
            .with(GRADE_RECORDED_ROUTING_KEY)
    }

    /**
     * Declare attendance_marked_queue
     */
    @Bean
    fun attendanceMarkedQueue(): Queue {
        return Queue(ATTENDANCE_MARKED_QUEUE, true, false, false)
    }

    /**
     * Bind attendance_marked_queue to exchange
     */
    @Bean
    fun attendanceMarkedBinding(): Binding {
        return BindingBuilder
            .bind(attendanceMarkedQueue())
            .to(exchange())
            .with(ATTENDANCE_MARKED_ROUTING_KEY)
    }

    /**
     * Declare attendance_session_approved_queue
     */
    @Bean
    fun attendanceSessionApprovedQueue(): Queue {
        return Queue(ATTENDANCE_SESSION_APPROVED_QUEUE, true, false, false)
    }

    /**
     * Bind attendance_session_approved_queue to exchange
     */
    @Bean
    fun attendanceSessionApprovedBinding(): Binding {
        return BindingBuilder
            .bind(attendanceSessionApprovedQueue())
            .to(exchange())
            .with(ATTENDANCE_SESSION_APPROVED_ROUTING_KEY)
    }

    /**
     * Declare user_created_queue
     */
    @Bean
    fun userCreatedQueue(): Queue {
        return Queue(USER_CREATED_QUEUE, true, false, false)
    }

    /**
     * Bind user_created_queue to exchange
     */
    @Bean
    fun userCreatedBinding(): Binding {
        return BindingBuilder
            .bind(userCreatedQueue())
            .to(exchange())
            .with(USER_CREATED_ROUTING_KEY)
    }

    /**
     * RabbitAdmin to ensure queues and bindings are created
     */
    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        val admin = RabbitAdmin(connectionFactory)
        admin.setAutoStartup(true)
        return admin
    }

    /**
     * JSON message converter
     */
    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    /**
     * Configure listener container factory with JSON converter
     */
    @Bean
    fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setMessageConverter(messageConverter())
        factory.setAutoStartup(true)
        return factory
    }
}

