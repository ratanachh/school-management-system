package com.visor.school.userservice.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.userservice.dto.UpdateStatusRequest;
import com.visor.school.userservice.dto.UpdateUserRequest;
import com.visor.school.userservice.dto.UserResponse;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.service.UserService;

import jakarta.validation.Valid;

/**
 * User management controller
 */
@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user by ID
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    /**
     * Update user information
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self
     * Note: Role updates are still restricted in the service layer
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userService.updateUser(
            id,
            request.firstName(),
            request.lastName(),
            request.phoneNumber(),
            request.roles()
        );

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user), "User updated successfully"));
    }

    /**
     * Update account status
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateAccountStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        User user = userService.updateAccountStatus(id, request.status());

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user), "Account status updated successfully"));
    }

    /**
     * Search users (simplified - in production would use pagination and filters)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String role
    ) {
        // Simplified implementation - in production, would use proper search with pagination
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "Search functionality not yet implemented"));
    }

    /**
     * List all administrators (SUPER_ADMIN only)
     */
    @GetMapping("/administrators")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAdministrators() {
        // For now, return empty list
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "Administrator list functionality not yet implemented"));
    }

    /**
     * Get administrator details by ID (SUPER_ADMIN only)
     */
    @GetMapping("/administrators/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('MANAGE_ADMINISTRATORS')")
    public ResponseEntity<ApiResponse<UserResponse>> getAdministrator(@PathVariable UUID id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Verify user is an administrator
        if (!user.hasRole(UserRole.ADMINISTRATOR) && !user.hasRole(UserRole.SUPER_ADMIN)) {
            // Using ApiResponse.error() factory
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User is not an administrator"));
        }

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }
}
