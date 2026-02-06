package com.visor.school.attendance.integration;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Disabled("Requires Docker environment - Fix Docker Desktop Pipe ID")
class CrossServiceCommunicationTest {



    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private org.springframework.amqp.core.AmqpAdmin amqpAdmin;

    @Container
    static final RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3-management")
            .withReuse(true);

    @org.springframework.test.context.DynamicPropertySource
    static void configureProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Test
    void shouldPublishAttendanceMarkedEventWhenAttendanceIsMarked() {
        // Setup RabbitMQ infrastructure for test
        String queueName = "test.attendance.marked";
        org.springframework.amqp.core.Queue queue = new org.springframework.amqp.core.Queue(queueName, false, false, true);
        org.springframework.amqp.core.TopicExchange exchange = new org.springframework.amqp.core.TopicExchange(com.visor.school.attendance.event.AttendanceEventPublisher.EXCHANGE_NAME);
        
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(org.springframework.amqp.core.BindingBuilder.bind(queue).to(exchange).with(com.visor.school.attendance.event.AttendanceEventPublisher.ATTENDANCE_MARKED_ROUTING_KEY));

        // Given
        UUID studentId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        // When
        AttendanceRecord attendance = attendanceService.markAttendanceDirectly(
            studentId,
            classId,
            LocalDate.now(),
            AttendanceStatus.PRESENT,
            teacherId,
            null
        );

        // Then - Verify attendance was marked
        assertNotNull(attendance);
        assertEquals(AttendanceStatus.PRESENT, attendance.getStatus());
        
        // Then - Verify event was published
        // We need to wait a small amount of time for the async message to be routed
        Object message = rabbitTemplate.receiveAndConvert(queueName, 2000);
        assertNotNull(message, "Should receive attendance marked event from RabbitMQ");
        
        // Optional: Verify message content if possible (depends on serialization)
        // System.out.println("Received message: " + message);
    }
}
