package com.visor.school.userservice.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.visor.school.common.events.BaseEvent;

public class UserCreatedEvent extends BaseEvent {
    private UUID userId;
    private String email;
    private Set<String> roles;
    private String firstName;
    private String lastName;
    private String keycloakId;

    public UserCreatedEvent() {
        super();
    }

    public UserCreatedEvent(UUID userId, String email, Set<String> roles, String firstName, String lastName, String keycloakId) {
        super(UUID.randomUUID(), Instant.now(), "1.0", "UserCreatedEvent");
        this.userId = userId;
        this.email = email;
        this.roles = roles;
        this.firstName = firstName;
        this.lastName = lastName;
        this.keycloakId = keycloakId;
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getKeycloakId() {
        return keycloakId;
    }
}
