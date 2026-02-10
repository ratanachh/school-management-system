package com.visor.school.userservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.model.Permission;
import com.visor.school.userservice.model.User;

/**
 * Service to sync permissions with Keycloak custom attributes
 * Permissions are stored in Keycloak as user attributes and included in JWT tokens
 */
@Service
public class KeycloakPermissionSyncService {

    private static final String PERMISSIONS_ATTRIBUTE_KEY = "permissions";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KeycloakClient keycloakClient;

    public KeycloakPermissionSyncService(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    /**
     * Sync permission to Keycloak (as a realm attribute or custom attribute)
     */
    public void syncPermissionToKeycloak(Permission permission) {
        logger.info("Syncing permission to Keycloak: {}", permission.getPermissionKey());
        // In production, this would create/update a realm attribute or role in Keycloak
        // For now, we'll just log it
    }

    /**
     * Sync user permission to Keycloak
     * Adds permission to user's custom attributes in Keycloak
     */
    public void syncUserPermissionToKeycloak(User user, Permission permission) {
        logger.info("Syncing user permission to Keycloak: user={}, permission={}", user.getId(), permission.getPermissionKey());

        try {
            // Get user from Keycloak
            UserRepresentation keycloakUser = keycloakClient.getUser(user.getKeycloakId());
            if (keycloakUser == null) {
                throw new IllegalStateException("User not found in Keycloak: " + user.getKeycloakId());
            }

            // Get existing permissions
            Map<String, List<String>> attributes = keycloakUser.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            } else {
                // Ensure attributes map is mutable
                attributes = new HashMap<>(attributes);
            }

            List<String> existingPermissions = attributes.get(PERMISSIONS_ATTRIBUTE_KEY);
            if (existingPermissions == null) {
                existingPermissions = new ArrayList<>();
            } else {
                existingPermissions = new ArrayList<>(existingPermissions);
            }

            // Add new permission if not already present
            if (!existingPermissions.contains(permission.getPermissionKey())) {
                existingPermissions.add(permission.getPermissionKey());
                attributes.put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions);
                keycloakUser.setAttributes(attributes);

                // Update user in Keycloak
                keycloakClient.updateUser(user.getKeycloakId(), keycloakUser);
                logger.info("Permission {} added to Keycloak user attributes", permission.getPermissionKey());
            }
        } catch (Exception e) {
            logger.error("Failed to sync permission to Keycloak for user: {}", user.getId(), e);
            // In production, consider retry mechanism or dead letter queue
        }
    }

    /**
     * Remove user permission from Keycloak
     */
    public void removeUserPermissionFromKeycloak(User user, Permission permission) {
        logger.info("Removing user permission from Keycloak: user={}, permission={}", user.getId(), permission.getPermissionKey());

        try {
            UserRepresentation keycloakUser = keycloakClient.getUser(user.getKeycloakId());
            if (keycloakUser == null) {
                throw new IllegalStateException("User not found in Keycloak: " + user.getKeycloakId());
            }

            Map<String, List<String>> attributes = keycloakUser.getAttributes();
            if (attributes == null) return; // No attributes, nothing to remove
            
            // Ensure mutable map
            attributes = new HashMap<>(attributes);

            List<String> existingPermissions = attributes.get(PERMISSIONS_ATTRIBUTE_KEY);
            if (existingPermissions == null) return; // No permissions, nothing to remove
            
            // Ensure mutable list
            existingPermissions = new ArrayList<>(existingPermissions);

            if (existingPermissions.remove(permission.getPermissionKey())) {
                attributes.put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions);
                keycloakUser.setAttributes(attributes);

                // Update user in Keycloak
                keycloakClient.updateUser(user.getKeycloakId(), keycloakUser);
                logger.info("Permission {} removed from Keycloak user attributes", permission.getPermissionKey());
            }
        } catch (Exception e) {
            logger.error("Failed to remove permission from Keycloak for user: {}", user.getId(), e);
        }
    }
}
