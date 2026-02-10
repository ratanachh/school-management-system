package com.visor.school.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
    @NotBlank String permissionKey,
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String category
) {}
