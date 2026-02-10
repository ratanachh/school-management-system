package com.visor.school.userservice.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;

/**
 * Service to extract current authenticated user information from Security Context
 */
@Service
public class SecurityContextService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;

    public SecurityContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt) {
                // Extract keycloakId from JWT claims
                String keycloakId = ((Jwt) principal).getSubject();
                if (keycloakId != null) {
                    return userRepository.findByKeycloakId(keycloakId).orElse(null);
                }
            } else if (principal instanceof String) {
                // If principal is a string (keycloakId)
                return userRepository.findByKeycloakId((String) principal).orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Failed to get current user from security context", e);
        }
        return null;
    }

    /**
     * Get current user's roles
     */
    public Set<UserRole> getCurrentUserRoles() {
        User user = getCurrentUser();
        return user != null ? user.getRoles() : Collections.emptySet();
    }

    /**
     * Get current user's ID
     */
    public UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(UserRole role) {
        User user = getCurrentUser();
        return user != null && user.hasRole(role);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(UserRole... roles) {
        User user = getCurrentUser();
        return user != null && user.hasAnyRole(roles);
    }

    /**
     * Check if current user has SUPER_ADMIN role
     */
    public boolean isSuperAdmin() {
        return hasRole(UserRole.SUPER_ADMIN);
    }

    /**
     * Check if current user has ADMINISTRATOR role
     */
    public boolean isAdministrator() {
        return hasRole(UserRole.ADMINISTRATOR);
    }

    /**
     * Check if current user has permission (from JWT claims)
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                String auth = authority.getAuthority();
                if (auth.equals(permission) ||
                    auth.equals("ROLE_" + permission) ||
                    auth.toLowerCase().contains(permission.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to check permission: " + permission, e);
        }
        return false;
    }

    /**
     * Check if current user has MANAGE_ADMINISTRATORS permission
     */
    public boolean canManageAdministrators() {
        return isSuperAdmin() || hasPermission("MANAGE_ADMINISTRATORS");
    }

    /**
     * Check if the provided user ID matches the current authenticated user's ID
     * Safe check that resolves the current user entity from DB via Keycloak ID
     */
    public boolean isCurrentUserId(UUID userId) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }
}
