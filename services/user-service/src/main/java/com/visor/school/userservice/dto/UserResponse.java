package com.visor.school.userservice.dto;

import java.util.Set;
import java.util.UUID;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    Set<UserRole> roles,
    boolean emailVerified,
    String accountStatus
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles(),
            user.isEmailVerified(),
            user.getAccountStatus() != null ? user.getAccountStatus().name() : null
        );
    }
}
