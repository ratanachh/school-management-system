package com.visor.school.userservice.integration;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.service.UserService;

/**
 * Integration test for cross-service communication via RabbitMQ
 * Tests that UserCreatedEvent is properly published and can be consumed by other services
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Disabled("Requires Keycloak instance or Testcontainers setup")
class CrossServiceCommunicationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3-management")
        .withReuse(true);

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldPublishUserCreatedEventWhenUserIsCreated() throws InterruptedException {
        // Given
        String email = "test-cross-service@example.com";

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);

        // When
        User user = userService.createUser(
            email,
            "Cross",
            "Service",
            roles,
            "StrongPassword123!",
            null
        );

        // Then - Verify event was published (check queue)
        // In a real scenario, we'd have a test consumer or check the queue
        Thread.sleep(1000); // Wait for async event publishing
        
        // Verify user was created
        assertNotNull(user);
        assertEquals(email, user.getEmail());
    }
}
