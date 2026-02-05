package com.visor.school.userservice.controller

import com.visor.school.common.api.ApiResponse
import com.visor.school.userservice.dto.UserResponse
import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * User management controller
 */
@RestController
@RequestMapping("/v1/users")
class UserController(
    private val userService: UserService
) {

    /**
     * Get user by ID
     */
    /**
     * Get user by ID
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    fun getUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)))
    }

    /**
     * Update user information
     * Authorization: SUPER_ADMIN or MANAGE_ADMINISTRATORS permission required for ADMINISTRATOR users
     */
    /**
     * Update user information
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self
     * Note: Role updates are still restricted in the service layer
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    fun updateUser(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.updateUser(
            id = id,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            roles = request.roles
        )

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user), "User updated successfully"))
    }

    /**
     * Update account status
     * Authorization: SUPER_ADMIN or MANAGE_ADMINISTRATORS permission required for ADMINISTRATOR users
     */
    /**
     * Update account status
     * Authorization: SUPER_ADMIN, MANAGE_ADMINISTRATORS, or Self (though typically users shouldn't change their own status to active without verification)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS') or @userService.canManageUser(#id) or @securityContextService.isCurrentUserId(#id)")
    fun updateAccountStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.updateAccountStatus(id, request.status)

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user), "Account status updated successfully"))
    }

    /**
     * Search users (simplified - in production would use pagination and filters)
     */
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) role: String?
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        // Simplified implementation - in production, would use proper search with pagination
        return ResponseEntity.ok(ApiResponse.success(emptyList(), "Search functionality not yet implemented"))
    }

    /**
     * List all administrators (SUPER_ADMIN only)
     */
    @GetMapping("/administrators")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS')")
    fun listAdministrators(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        // In production, would query by role = ADMINISTRATOR
        // For now, return empty list - implementation would use UserRepository.findByRole(UserRole.ADMINISTRATOR)
        return ResponseEntity.ok(ApiResponse.success(emptyList(), "Administrator list functionality not yet implemented"))
    }

    /**
     * Get administrator details by ID (SUPER_ADMIN only)
     */
    @GetMapping("/administrators/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS')")
    fun getAdministrator(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()

        // Verify user is an administrator
        if (!user.hasRole(UserRole.ADMINISTRATOR) && !user.hasRole(UserRole.SUPER_ADMIN)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User is not an administrator"))
        }

        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)))
    }
}

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val roles: Set<UserRole>? = null
)

data class UpdateStatusRequest(
    val status: AccountStatus
)

