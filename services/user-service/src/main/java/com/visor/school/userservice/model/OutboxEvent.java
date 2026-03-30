package com.visor.school.userservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_events_status_next_attempt", columnList = "status,next_attempt_at"),
    @Index(name = "idx_outbox_events_created_at", columnList = "created_at")
})
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "exchange_name", nullable = false, length = 200)
    private String exchangeName;

    @Column(name = "routing_key", nullable = false, length = 200)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OutboxEvent() {}

    public static OutboxEvent pending(
        String eventType,
        String exchangeName,
        String routingKey,
        String payload,
        Instant now
    ) {
        OutboxEvent event = new OutboxEvent();
        event.id = UUID.randomUUID();
        event.eventType = eventType;
        event.exchangeName = exchangeName;
        event.routingKey = routingKey;
        event.payload = payload;
        event.status = OutboxStatus.PENDING;
        event.attempts = 0;
        event.nextAttemptAt = now;
        event.createdAt = now;
        event.updatedAt = now;
        return event;
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void markFailed(Instant now, Instant nextAttemptAt, int maxAttempts, String errorMessage) {
        this.attempts += 1;
        this.lastError = errorMessage;
        this.nextAttemptAt = nextAttemptAt;
        this.updatedAt = now;
        this.status = this.attempts >= maxAttempts ? OutboxStatus.DEAD : OutboxStatus.PENDING;
    }
}
