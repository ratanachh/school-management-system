package com.visor.school.userservice.service

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.model.Permission
import com.visor.school.userservice.model.User
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service to sync permissions with Keycloak custom attributes
 * Permissions are stored in Keycloak as user attributes and included in JWT tokens
 */
@Service
class KeycloakPermissionSyncService(
    private val keycloakClient: KeycloakClient
) {
    private val logger = LoggerFactory.getLogger(KeycloakPermissionSyncService::class.java)

    companion object {
        const val PERMISSIONS_ATTRIBUTE_KEY = "permissions"
    }

    /**
     * Sync permission to Keycloak (as a realm attribute or custom attribute)
     */
    fun syncPermissionToKeycloak(permission: Permission) {
        logger.info("Syncing permission to Keycloak: ${permission.permissionKey}")
        // In production, this would create/update a realm attribute or role in Keycloak
        // For now, we'll just log it
        // Example implementation:
        // keycloakClient.addRealmAttribute(permission.permissionKey, permission.description)
    }

    /**
     * Sync user permission to Keycloak
     * Adds permission to user's custom attributes in Keycloak
     */
    fun syncUserPermissionToKeycloak(user: User, permission: Permission) {
        logger.info("Syncing user permission to Keycloak: user=${user.id}, permission=${permission.permissionKey}")

        try {
            // Get user from Keycloak
            val keycloakUser = keycloakClient.getUser(user.keycloakId)
                ?: throw IllegalStateException("User not found in Keycloak: ${user.keycloakId}")

            // Get existing permissions
            val existingPermissions = keycloakUser.attributes?.get(PERMISSIONS_ATTRIBUTE_KEY)?.toMutableList()
                ?: mutableListOf()

            // Add new permission if not already present
            if (!existingPermissions.contains(permission.permissionKey)) {
                existingPermissions.add(permission.permissionKey)
                keycloakUser.attributes = keycloakUser.attributes?.apply {
                    put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions)
                } ?: mapOf(PERMISSIONS_ATTRIBUTE_KEY to existingPermissions)

                // Update user in Keycloak
                keycloakClient.updateUser(user.keycloakId, keycloakUser)
                logger.info("Permission ${permission.permissionKey} added to Keycloak user attributes")
            }
        } catch (e: Exception) {
            logger.error("Failed to sync permission to Keycloak for user: ${user.id}", e)
            // In production, consider retry mechanism or dead letter queue
        }
    }

    /**
     * Remove user permission from Keycloak
     */
    fun removeUserPermissionFromKeycloak(user: User, permission: Permission) {
        logger.info("Removing user permission from Keycloak: user=${user.id}, permission=${permission.permissionKey}")

        try {
            val keycloakUser = keycloakClient.getUser(user.keycloakId)
                ?: throw IllegalStateException("User not found in Keycloak: ${user.keycloakId}")

            val existingPermissions = keycloakUser.attributes?.get(PERMISSIONS_ATTRIBUTE_KEY)?.toMutableList()
                ?: mutableListOf()

            if (existingPermissions.remove(permission.permissionKey)) {
                keycloakUser.attributes = keycloakUser.attributes?.apply {
                    put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions)
                } ?: mapOf(PERMISSIONS_ATTRIBUTE_KEY to existingPermissions)

                // Update user in Keycloak
                keycloakClient.updateUser(user.keycloakId, keycloakUser)
                logger.info("Permission ${permission.permissionKey} removed from Keycloak user attributes")
            }
        } catch (e: Exception) {
            logger.error("Failed to remove permission from Keycloak for user: ${user.id}", e)
        }
    }
}

