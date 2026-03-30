package com.visor.school.keycloak.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KeycloakPermissionSyncService {

    private static final String PERMISSIONS_ATTRIBUTE_KEY = "permissions";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KeycloakClient keycloakClient;

    public KeycloakPermissionSyncService(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public void syncPermissionToKeycloak(String permissionKey) {
        logger.info("Syncing permission to Keycloak: {}", permissionKey);
    }

    public void syncUserPermissionToKeycloak(String keycloakId, String permissionKey) {
        logger.info("Syncing user permission to Keycloak: keycloakId={}, permission={}", keycloakId, permissionKey);
        UserRepresentation keycloakUser = keycloakClient.getUser(keycloakId);
        if (keycloakUser == null) {
            throw new KeycloakException("User not found in Keycloak: " + keycloakId);
        }

        Map<String, List<String>> attributes = keycloakUser.getAttributes();
        attributes = attributes == null ? new HashMap<>() : new HashMap<>(attributes);
        List<String> existingPermissions = attributes.get(PERMISSIONS_ATTRIBUTE_KEY);
        existingPermissions = existingPermissions == null ? new ArrayList<>() : new ArrayList<>(existingPermissions);

        if (!existingPermissions.contains(permissionKey)) {
            existingPermissions.add(permissionKey);
            attributes.put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions);
            keycloakUser.setAttributes(attributes);
            keycloakClient.updateUser(keycloakId, keycloakUser);
            logger.info("Permission {} added to Keycloak user attributes", permissionKey);
        }
    }

    public void removeUserPermissionFromKeycloak(String keycloakId, String permissionKey) {
        logger.info("Removing user permission from Keycloak: keycloakId={}, permission={}", keycloakId, permissionKey);
        UserRepresentation keycloakUser = keycloakClient.getUser(keycloakId);
        if (keycloakUser == null) {
            throw new KeycloakException("User not found in Keycloak: " + keycloakId);
        }
        Map<String, List<String>> attributes = keycloakUser.getAttributes();
        if (attributes == null) return;
        attributes = new HashMap<>(attributes);
        List<String> existingPermissions = attributes.get(PERMISSIONS_ATTRIBUTE_KEY);
        if (existingPermissions == null) return;
        existingPermissions = new ArrayList<>(existingPermissions);
        if (existingPermissions.remove(permissionKey)) {
            attributes.put(PERMISSIONS_ATTRIBUTE_KEY, existingPermissions);
            keycloakUser.setAttributes(attributes);
            keycloakClient.updateUser(keycloakId, keycloakUser);
            logger.info("Permission {} removed from Keycloak user attributes", permissionKey);
        }
    }
}
