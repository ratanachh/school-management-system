package com.visor.school.userservice.event;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;

/**
 * Publisher for user-related events to RabbitMQ
 * Implements retry logic with exponential backoff for failed event publishing
 */
@Component
public class UserEventPublisher {

    private static final String EXCHANGE_NAME = "school-management.exchange";
    private static final String USER_CREATED_ROUTING_KEY = "user.created";
    private static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    private static final String USER_EMAIL_VERIFIED_ROUTING_KEY = "user.email.verified";

    // Retry configuration constants
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 100L;
    private static final long MAX_RETRY_DELAY_MS = 2000L;
    private static final double RETRY_MULTIPLIER = 2.0;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RabbitTemplate rabbitTemplate;
    private final String serviceName;

    public UserEventPublisher(RabbitTemplate rabbitTemplate, @Value("${spring.application.name}") String serviceName) {
        this.rabbitTemplate = rabbitTemplate;
        this.serviceName = serviceName;
    }

    /**
     * Publish user created event with retry logic
     */
    public void publishUserCreated(User user) {
        Set<String> roles = user.getRoles().stream()
            .map(UserRole::name)
            .collect(Collectors.toSet());

        UserCreatedEvent event = new UserCreatedEvent(
            user.getId(),
            user.getEmail(),
            roles,
            user.getFirstName(),
            user.getLastName(),
            user.getKeycloakId()
        );

        publishWithRetry(
            event,
            USER_CREATED_ROUTING_KEY,
            "user.created",
            user.getId()
        );
    }

    /**
     * Publish user updated event with retry logic
     */
    public void publishUserUpdated(User user) {
        Set<String> roles = user.getRoles().stream()
            .map(UserRole::name)
            .collect(Collectors.toSet());

        UserUpdatedEvent event = new UserUpdatedEvent(
            user.getId(),
            user.getEmail(),
            roles,
            user.getAccountStatus() != null ? user.getAccountStatus().name() : null
        );

        publishWithRetry(
            event,
            USER_UPDATED_ROUTING_KEY,
            "user.updated",
            user.getId()
        );
    }

    /**
     * Publish email verified event with retry logic
     */
    public void publishEmailVerified(User user) {
        EmailVerifiedEvent event = new EmailVerifiedEvent(
            user.getId(),
            user.getEmail()
        );

        publishWithRetry(
            event,
            USER_EMAIL_VERIFIED_ROUTING_KEY,
            "user.email.verified",
            user.getId()
        );
    }

    /**
     * Publish event with retry logic and exponential backoff
     */
    private void publishWithRetry(
        Object event,
        String routingKey,
        String eventType,
        UUID userId
    ) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, event);
                logger.info("Published {} event for user: {}", eventType, userId);
                return; // Success - exit retry loop
            } catch (AmqpException e) {
                lastException = e;
                attempt++;

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    long delay = calculateRetryDelay(attempt);
                    logger.warn(
                        "Failed to publish {} event for user: {} (attempt {}/{}). Retrying in {}ms. Error: {}",
                        eventType, userId, attempt, MAX_RETRY_ATTEMPTS, delay, e.getMessage()
                    );
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    logger.error(
                        "Failed to publish {} event for user: {} after {} attempts. " +
                        "Event will not be published. Consider implementing a dead letter queue or event store for later retry.",
                        eventType, userId, MAX_RETRY_ATTEMPTS, e
                    );
                }
            } catch (Exception e) {
                lastException = e;
                attempt++;

                // For non-AMQP exceptions, log and don't retry
                logger.error(
                    "Unexpected error publishing {} event for user: {}. Error type: {}, Message: {}",
                    eventType, userId, e.getClass().getSimpleName(), e.getMessage(), e
                );
                return; // Don't retry for unexpected exceptions
            }
        }

        // All retries exhausted - log final error
        if (lastException != null) {
            logger.error(
                "Failed to publish {} event for user: {} after all retry attempts. " +
                "This is a non-critical failure and does not affect user creation. " +
                "Consider implementing an event store or dead letter queue for failed events.",
                eventType, userId, lastException
            );
        }
    }

    private long calculateRetryDelay(int attempt) {
        long delay = (long) (INITIAL_RETRY_DELAY_MS * Math.pow(RETRY_MULTIPLIER, attempt - 1));
        return Math.min(delay, MAX_RETRY_DELAY_MS);
    }
}
