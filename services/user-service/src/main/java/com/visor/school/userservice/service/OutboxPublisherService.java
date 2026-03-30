package com.visor.school.userservice.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.userservice.model.OutboxEvent;
import com.visor.school.userservice.model.OutboxStatus;
import com.visor.school.userservice.repository.OutboxEventRepository;

@Service
public class OutboxPublisherService {

    private static final double RETRY_MULTIPLIER = 2.0;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public OutboxPublisherService(
        OutboxEventRepository outboxEventRepository,
        RabbitTemplate rabbitTemplate,
        ObjectMapper objectMapper,
        @Value("${outbox.publisher.max-attempts:12}") int maxAttempts,
        @Value("${outbox.publisher.initial-backoff-ms:1000}") long initialBackoffMs,
        @Value("${outbox.publisher.max-backoff-ms:60000}") long maxBackoffMs
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.maxAttempts = maxAttempts;
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        Instant now = Instant.now();
        List<OutboxEvent> events = outboxEventRepository
            .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus.PENDING, now);

        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            publishOne(event);
        }
    }

    private void publishOne(OutboxEvent event) {
        Instant now = Instant.now();
        try {
            Object payloadObject = objectMapper.readTree(event.getPayload());
            rabbitTemplate.convertAndSend(
                event.getExchangeName(),
                event.getRoutingKey(),
                payloadObject
            );
            event.markPublished(now);
            logger.info("Published outbox event {} [{}]", event.getId(), event.getEventType());
        } catch (JsonProcessingException ex) {
            event.markFailed(now, now, 1, "Invalid outbox payload: " + ex.getMessage());
            logger.error("Outbox event {} has invalid payload and is marked DEAD", event.getId(), ex);
        } catch (AmqpException ex) {
            long delayMs = calculateRetryDelay(event.getAttempts());
            Instant nextAttemptAt = now.plusMillis(delayMs);
            event.markFailed(now, nextAttemptAt, maxAttempts, ex.getMessage());
            logger.warn(
                "Outbox publish failed for {} [{}], attempt {}/{}. Next attempt at {}",
                event.getId(),
                event.getEventType(),
                event.getAttempts(),
                maxAttempts,
                nextAttemptAt,
                ex
            );
        }
    }

    private long calculateRetryDelay(int previousAttempts) {
        int exponent = Math.max(previousAttempts, 0);
        long delay = (long) (initialBackoffMs * Math.pow(RETRY_MULTIPLIER, exponent));
        return Math.min(delay, maxBackoffMs);
    }
}
