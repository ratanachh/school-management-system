package com.visor.school.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the School Management System
 */
public abstract class BaseEvent {

    private final UUID eventId;
    private final Instant timestamp;
    private final String version;
    private final String eventType;

    protected BaseEvent() {
        this(UUID.randomUUID(), Instant.now(), "1.0", "");
    }

    protected BaseEvent(UUID eventId, Instant timestamp, String version) {
        this(eventId, timestamp, version, "");
    }

    protected BaseEvent(UUID eventId, Instant timestamp, String version, String eventType) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.version = version != null ? version : "1.0";
        this.eventType = eventType != null ? eventType : "";
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getVersion() {
        return version;
    }

    public String getEventType() {
        return eventType;
    }

    public abstract UUID getAggregateId();

    public abstract String getAggregateType();
}
