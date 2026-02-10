package com.visor.school.userservice.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.model.Permission;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserPermission;
import com.visor.school.userservice.repository.PermissionRepository;
import com.visor.school.userservice.repository.UserPermissionRepository;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Permission service for fine-grained access control
 */
@Service
@Transactional
public class PermissionService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserRepository userRepository;
    private final KeycloakPermissionSyncService keycloakPermissionSyncService;

    public PermissionService(
        PermissionRepository permissionRepository,
        UserPermissionRepository userPermissionRepository,
        UserRepository userRepository,
        KeycloakPermissionSyncService keycloakPermissionSyncService
    ) {
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.userRepository = userRepository;
        this.keycloakPermissionSyncService = keycloakPermissionSyncService;
    }

    /**
     * Create a new permission
     */
    public Permission createPermission(
        String permissionKey,
        String name,
        String description,
        String category
    ) {
        logger.info("Creating permission: {}", permissionKey);

        if (permissionRepository.existsByPermissionKey(permissionKey)) {
            throw new IllegalArgumentException("Permission with key " + permissionKey + " already exists");
        }

        Permission permission = new Permission(
            permissionKey.toUpperCase(),
            name,
            description,
            category
        );

        Permission saved = permissionRepository.save(permission);

        // Sync with Keycloak
        keycloakPermissionSyncService.syncPermissionToKeycloak(saved);

        logger.info("Permission created: {}", saved.getId());
        return saved;
    }

    /**
     * Assign permission to user
     */
    public UserPermission assignToUser(UUID userId, UUID permissionId, UUID assignedBy) {
        logger.info("Assigning permission {} to user {}", permissionId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        if (userPermissionRepository.existsByUserIdAndPermissionId(userId, permissionId)) {
            throw new IllegalArgumentException("Permission already assigned to user");
        }

        UserPermission userPermission = new UserPermission(
            user,
            permission,
            assignedBy
        );

        UserPermission saved = userPermissionRepository.save(userPermission);

        // Sync with Keycloak
        keycloakPermissionSyncService.syncUserPermissionToKeycloak(user, permission);

        logger.info("Permission assigned to user: {}", saved.getId());
        return saved;
    }

    /**
     * Get permissions for a user
     */
    @Transactional(readOnly = true)
    public List<Permission> getByUser(UUID userId) {
        List<UserPermission> userPermissions = userPermissionRepository.findByUserId(userId);
        return userPermissions.stream()
            .map(UserPermission::getPermission)
            .collect(Collectors.toList());
    }

    /**
     * Get permission by key
     */
    @Transactional(readOnly = true)
    public Permission findByKey(String permissionKey) {
        return permissionRepository.findByPermissionKey(permissionKey.toUpperCase()).orElse(null);
    }

    /**
     * Get all permissions
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Get permissions by category
     */
    @Transactional(readOnly = true)
    public List<Permission> getByCategory(String category) {
        return permissionRepository.findByCategory(category);
    }

    /**
     * Remove permission from user
     */
    public void removeFromUser(UUID userId, UUID permissionId) {
        logger.info("Removing permission {} from user {}", permissionId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        userPermissionRepository.deleteByUserIdAndPermissionId(userId, permissionId);

        // Sync removal with Keycloak
        keycloakPermissionSyncService.removeUserPermissionFromKeycloak(user, permission);

        logger.info("Permission removed from user");
    }
}
