package com.visor.school.userservice.service

import com.visor.school.userservice.model.Permission
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserPermission
import com.visor.school.userservice.repository.PermissionRepository
import com.visor.school.userservice.repository.UserPermissionRepository
import com.visor.school.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Permission service for fine-grained access control
 */
@Service
@Transactional
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val userRepository: UserRepository,
    private val keycloakPermissionSyncService: KeycloakPermissionSyncService
) {
    private val logger = LoggerFactory.getLogger(PermissionService::class.java)

    /**
     * Create a new permission
     */
    fun createPermission(
        permissionKey: String,
        name: String,
        description: String,
        category: String
    ): Permission {
        logger.info("Creating permission: $permissionKey")

        if (permissionRepository.existsByPermissionKey(permissionKey)) {
            throw IllegalArgumentException("Permission with key $permissionKey already exists")
        }

        val permission = Permission(
            permissionKey = permissionKey.uppercase(),
            name = name,
            description = description,
            category = category
        )

        val saved = permissionRepository.save(permission)

        // Sync with Keycloak
        keycloakPermissionSyncService.syncPermissionToKeycloak(saved)

        logger.info("Permission created: ${saved.id}")
        return saved
    }

    /**
     * Assign permission to user
     */
    fun assignToUser(userId: UUID, permissionId: UUID, assignedBy: UUID? = null): UserPermission {
        logger.info("Assigning permission $permissionId to user $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val permission = permissionRepository.findById(permissionId)
            .orElseThrow { IllegalArgumentException("Permission not found: $permissionId") }

        if (userPermissionRepository.existsByUserIdAndPermissionId(userId, permissionId)) {
            throw IllegalArgumentException("Permission already assigned to user")
        }

        val userPermission = UserPermission(
            user = user,
            permission = permission,
            assignedBy = assignedBy
        )

        val saved = userPermissionRepository.save(userPermission)

        // Sync with Keycloak
        keycloakPermissionSyncService.syncUserPermissionToKeycloak(user, permission)

        logger.info("Permission assigned to user: ${saved.id}")
        return saved
    }

    /**
     * Get permissions for a user
     */
    @Transactional(readOnly = true)
    fun getByUser(userId: UUID): List<Permission> {
        val userPermissions = userPermissionRepository.findByUserId(userId)
        return userPermissions.map { it.permission }
    }

    /**
     * Get permission by key
     */
    @Transactional(readOnly = true)
    fun findByKey(permissionKey: String): Permission? {
        return permissionRepository.findByPermissionKey(permissionKey.uppercase()).orElse(null)
    }

    /**
     * Get all permissions
     */
    @Transactional(readOnly = true)
    fun getAllPermissions(): List<Permission> {
        return permissionRepository.findAll()
    }

    /**
     * Get permissions by category
     */
    @Transactional(readOnly = true)
    fun getByCategory(category: String): List<Permission> {
        return permissionRepository.findByCategory(category)
    }

    /**
     * Remove permission from user
     */
    fun removeFromUser(userId: UUID, permissionId: UUID) {
        logger.info("Removing permission $permissionId from user $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val permission = permissionRepository.findById(permissionId)
            .orElseThrow { IllegalArgumentException("Permission not found: $permissionId") }

        userPermissionRepository.deleteByUserIdAndPermissionId(userId, permissionId)

        // Sync removal with Keycloak
        keycloakPermissionSyncService.removeUserPermissionFromKeycloak(user, permission)

        logger.info("Permission removed from user")
    }
}

