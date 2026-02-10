package com.visor.school.userservice.event;

import java.time.Instant;
import java.util.UUID;

import com.visor.school.common.events.BaseEvent;

public class EmailVerifiedEvent extends BaseEvent {
    private UUID userId;
    private String email;

    public EmailVerifiedEvent() {
        super();
    }

    public EmailVerifiedEvent(UUID userId, String email) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "EmailVerifiedEvent");
        this.userId = userId;
        this.email = email;
    }

    @Override
    public UUID getAggregateId() {
        return userId;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
