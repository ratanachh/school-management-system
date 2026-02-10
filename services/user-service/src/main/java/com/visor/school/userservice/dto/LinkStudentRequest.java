package com.visor.school.userservice.dto;

import com.visor.school.userservice.model.Relationship;

import jakarta.validation.constraints.NotNull;

public record LinkStudentRequest(
    @NotNull Relationship relationship,
    Boolean isPrimary
) {}
