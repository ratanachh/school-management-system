package com.visor.school.userservice.dto;

import java.util.UUID;

import com.visor.school.userservice.model.Permission;

public record PermissionResponse(
    UUID id,
    String permissionKey,
    String name,
    String description,
    String category
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
            permission.getId(),
            permission.getPermissionKey(),
            permission.getName(),
            permission.getDescription(),
            permission.getCategory()
        );
    }
}
