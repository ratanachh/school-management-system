package com.visor.school.userservice.controller

import com.visor.school.common.api.ApiResponse
import com.visor.school.userservice.model.Permission
import com.visor.school.userservice.service.PermissionService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Permission management controller
 * Requires ADMINISTRATOR role for permission management operations
 */
@RestController
@RequestMapping("/api/v1/permissions")
class PermissionController(
    private val permissionService: PermissionService
) {

    /**
     * Create a new permission
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun createPermission(@Valid @RequestBody request: CreatePermissionRequest): ResponseEntity<ApiResponse<PermissionResponse>> {
        val permission = permissionService.createPermission(
            permissionKey = request.permissionKey,
            name = request.name,
            description = request.description,
            category = request.category
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(PermissionResponse.from(permission), "Permission created successfully"))
    }

    /**
     * Get all permissions
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun getAllPermissions(): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionService.getAllPermissions()
        return ResponseEntity.ok(ApiResponse.success(permissions.map { PermissionResponse.from(it) }))
    }

    /**
     * Get permission by key
     */
    @GetMapping("/{permissionKey}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun getPermission(@PathVariable permissionKey: String): ResponseEntity<ApiResponse<PermissionResponse>> {
        val permission = permissionService.findByKey(permissionKey)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(PermissionResponse.from(permission)))
    }

    /**
     * Get permissions by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun getPermissionsByCategory(@PathVariable category: String): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionService.getByCategory(category)
        return ResponseEntity.ok(ApiResponse.success(permissions.map { PermissionResponse.from(it) }))
    }

    /**
     * Assign permission to user
     */
    @PostMapping("/{permissionId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun assignPermissionToUser(
        @PathVariable permissionId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        permissionService.assignToUser(userId, permissionId)

        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf(
                    "message" to "Permission assigned successfully",
                    "userId" to userId.toString(),
                    "permissionId" to permissionId.toString()
                )
            )
        )
    }

    /**
     * Get permissions for a user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or #userId == authentication.principal.id")
    fun getUserPermissions(@PathVariable userId: UUID): ResponseEntity<ApiResponse<List<PermissionResponse>>> {
        val permissions = permissionService.getByUser(userId)
        return ResponseEntity.ok(ApiResponse.success(permissions.map { PermissionResponse.from(it) }))
    }

    /**
     * Remove permission from user
     */
    @DeleteMapping("/{permissionId}/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun removePermissionFromUser(
        @PathVariable permissionId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        permissionService.removeFromUser(userId, permissionId)

        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("message" to "Permission removed successfully")
            )
        )
    }
}

// Request DTOs
data class CreatePermissionRequest(
    @field:NotBlank
    val permissionKey: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val description: String,

    @field:NotBlank
    val category: String
)

// Response DTOs
data class PermissionResponse(
    val id: UUID,
    val permissionKey: String,
    val name: String,
    val description: String,
    val category: String
) {
    companion object {
        fun from(permission: Permission): PermissionResponse {
            return PermissionResponse(
                id = permission.id,
                permissionKey = permission.permissionKey,
                name = permission.name,
                description = permission.description,
                category = permission.category
            )
        }
    }
}

