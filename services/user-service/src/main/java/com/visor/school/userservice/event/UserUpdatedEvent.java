package com.visor.school.userservice.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.visor.school.common.events.BaseEvent;

public class UserUpdatedEvent extends BaseEvent {
    private UUID userId;
    private String email;
    private Set<String> roles;
    private String accountStatus;

    public UserUpdatedEvent() {
        super();
    }

    public UserUpdatedEvent(UUID userId, String email, Set<String> roles, String accountStatus) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "UserUpdatedEvent");
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.accountStatus = accountStatus;
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

    public Set<String> getRoles() {
        return roles;
    }

    public String getAccountStatus() {
        return accountStatus;
    }
}
