package com.visor.school.userservice.event;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.userservice.model.OutboxEvent;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.OutboxEventRepository;

/**
 * Publisher for user-related events to RabbitMQ.
 */
@Component
public class UserEventPublisher {

    private static final String EXCHANGE_NAME = "school-management.exchange";
    private static final String USER_CREATED_ROUTING_KEY = "user.created";
    private static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    private static final String USER_EMAIL_VERIFIED_ROUTING_KEY = "user.email.verified";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

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

        queueOutboxEvent(
            event,
            USER_CREATED_ROUTING_KEY,
            "user.created",
            user.getId()
        );
    }

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

        queueOutboxEvent(
            event,
            USER_UPDATED_ROUTING_KEY,
            "user.updated",
            user.getId()
        );
    }

    public void publishEmailVerified(User user) {
        EmailVerifiedEvent event = new EmailVerifiedEvent(
            user.getId(),
            user.getEmail()
        );

        queueOutboxEvent(
            event,
            USER_EMAIL_VERIFIED_ROUTING_KEY,
            "user.email.verified",
            user.getId()
        );
    }

    private void queueOutboxEvent(
        Object event,
        String routingKey,
        String eventType,
        UUID userId
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.pending(
                eventType,
                EXCHANGE_NAME,
                routingKey,
                payload,
                java.time.Instant.now()
            );
            Objects.requireNonNull(outboxEventRepository.save(outboxEvent));
            logger.info("Queued {} event for user {} in outbox with id {}", eventType, userId, outboxEvent.getId());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize " + eventType + " event for user " + userId, ex);
        }
    }
}
