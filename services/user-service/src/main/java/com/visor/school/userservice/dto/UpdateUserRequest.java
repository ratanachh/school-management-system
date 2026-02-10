package com.visor.school.userservice.dto;

import java.util.Set;

import com.visor.school.userservice.model.UserRole;

public record UpdateUserRequest(
    String firstName,
    String lastName,
    String phoneNumber,
    Set<UserRole> roles
) {}
