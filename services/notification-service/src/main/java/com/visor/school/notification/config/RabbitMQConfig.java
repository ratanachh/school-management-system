package com.visor.school.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for notification service
 * Auto-declares queues and bindings for event consumption
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "school-management.exchange";
    
    // Queue names
    public static final String GRADE_RECORDED_QUEUE = "grade_recorded_queue";
    public static final String ATTENDANCE_MARKED_QUEUE = "attendance_marked_queue";
    public static final String ATTENDANCE_SESSION_APPROVED_QUEUE = "attendance_session_approved_queue";
    public static final String USER_CREATED_QUEUE = "user_created_queue";
    
    // Routing keys
    public static final String GRADE_RECORDED_ROUTING_KEY = "grade.recorded";
    public static final String ATTENDANCE_MARKED_ROUTING_KEY = "attendance.marked";
    public static final String ATTENDANCE_SESSION_APPROVED_ROUTING_KEY = "attendance.session.approved";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";

    /**
     * Declare the topic exchange
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Declare grade_recorded_queue
     */
    @Bean
    public Queue gradeRecordedQueue() {
        return new Queue(GRADE_RECORDED_QUEUE, true, false, false);
    }

    /**
     * Bind grade_recorded_queue to exchange
     */
    @Bean
    public Binding gradeRecordedBinding() {
        return BindingBuilder
            .bind(gradeRecordedQueue())
            .to(exchange())
            .with(GRADE_RECORDED_ROUTING_KEY);
    }

    /**
     * Declare attendance_marked_queue
     */
    @Bean
    public Queue attendanceMarkedQueue() {
        return new Queue(ATTENDANCE_MARKED_QUEUE, true, false, false);
    }

    /**
     * Bind attendance_marked_queue to exchange
     */
    @Bean
    public Binding attendanceMarkedBinding() {
        return BindingBuilder
            .bind(attendanceMarkedQueue())
            .to(exchange())
            .with(ATTENDANCE_MARKED_ROUTING_KEY);
    }

    /**
     * Declare attendance_session_approved_queue
     */
    @Bean
    public Queue attendanceSessionApprovedQueue() {
        return new Queue(ATTENDANCE_SESSION_APPROVED_QUEUE, true, false, false);
    }

    /**
     * Bind attendance_session_approved_queue to exchange
     */
    @Bean
    public Binding attendanceSessionApprovedBinding() {
        return BindingBuilder
            .bind(attendanceSessionApprovedQueue())
            .to(exchange())
            .with(ATTENDANCE_SESSION_APPROVED_ROUTING_KEY);
    }

    /**
     * Declare user_created_queue
     */
    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true, false, false);
    }

    /**
     * Bind user_created_queue to exchange
     */
    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
            .bind(userCreatedQueue())
            .to(exchange())
            .with(USER_CREATED_ROUTING_KEY);
    }

    /**
     * RabbitAdmin to ensure queues and bindings are created
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    /**
     * JSON message converter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure listener container factory with JSON converter
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAutoStartup(true);
        return factory;
    }
}
