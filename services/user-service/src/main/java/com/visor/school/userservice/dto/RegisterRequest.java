package com.visor.school.userservice.dto;

import java.util.Set;

import com.visor.school.userservice.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @Email @NotBlank String email,
    @NotBlank String firstName,
    @NotBlank String lastName,
    Set<UserRole> roles,
    @NotBlank String password,
    String phoneNumber
) {}
