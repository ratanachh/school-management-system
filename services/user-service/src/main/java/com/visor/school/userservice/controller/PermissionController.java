package com.visor.school.userservice.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.userservice.dto.CreatePermissionRequest;
import com.visor.school.userservice.dto.PermissionResponse;
import com.visor.school.userservice.model.Permission;
import com.visor.school.userservice.service.PermissionService;

import jakarta.validation.Valid;

/**
 * Permission management controller
 * Requires ADMINISTRATOR role for permission management operations
 */
@RestController
@RequestMapping("/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Create a new permission
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
            request.permissionKey(),
            request.name(),
            request.description(),
            request.category()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(PermissionResponse.from(permission), "Permission created successfully"));
    }

    /**
     * Get all permissions
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions.stream().map(PermissionResponse::from).collect(Collectors.toList())));
    }

    /**
     * Get permission by key
     */
    @GetMapping("/{permissionKey}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable String permissionKey) {
        Permission permission = permissionService.findByKey(permissionKey);
        if (permission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(PermissionResponse.from(permission)));
    }

    /**
     * Get permissions by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByCategory(@PathVariable String category) {
        List<Permission> permissions = permissionService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(permissions.stream().map(PermissionResponse::from).collect(Collectors.toList())));
    }

    /**
     * Assign permission to user
     */
    @PostMapping("/{permissionId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> assignPermissionToUser(
        @PathVariable UUID permissionId,
        @PathVariable UUID userId
    ) {
        // assignedBy logic omitted in Kotlin controller, likely not passed or handled by service if overloaded? 
        // Service: assignToUser(userId, permissionId, assignedBy)
        // Kotlin controller: permissionService.assignToUser(userId, permissionId)
        // Kotlin service parameter: assignedBy: UUID? = null
        // My Java service matches: assignToUser(userId, permissionId, assignedBy)
        // I'll pass null for assignedBy as in Kotlin.
        
        permissionService.assignToUser(userId, permissionId, null);

        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of(
                    "message", "Permission assigned successfully",
                    "userId", userId.toString(),
                    "permissionId", permissionId.toString()
                )
            )
        );
    }

    /**
     * Get permissions for a user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or @securityContextService.isCurrentUserId(#userId)")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getUserPermissions(@PathVariable UUID userId) {
        List<Permission> permissions = permissionService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(permissions.stream().map(PermissionResponse::from).collect(Collectors.toList())));
    }

    /**
     * Remove permission from user
     */
    @DeleteMapping("/{permissionId}/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> removePermissionFromUser(
        @PathVariable UUID permissionId,
        @PathVariable UUID userId
    ) {
        permissionService.removeFromUser(userId, permissionId);

        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of("message", "Permission removed successfully")
            )
        );
    }
}
