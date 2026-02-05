package com.visor.school.userservice.service

import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service to extract current authenticated user information from Security Context
 */
@Service
class SecurityContextService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(SecurityContextService::class.java)

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return null
        }

        return try {
            val principal = authentication.principal
            when (principal) {
                is Jwt -> {
                    // Extract keycloakId from JWT claims
                    val keycloakId = principal.subject
                    if (keycloakId != null) {
                        userRepository.findByKeycloakId(keycloakId).orElse(null)
                    } else {
                        null
                    }
                }
                is String -> {
                    // If principal is a string (keycloakId)
                    userRepository.findByKeycloakId(principal).orElse(null)
                }
                else -> null
            }
        } catch (e: Exception) {
            logger.warn("Failed to get current user from security context", e)
            null
        }
    }

    /**
     * Get current user's roles
     */
    fun getCurrentUserRoles(): Set<UserRole> {
        val user = getCurrentUser()
        return user?.roles?.toSet() ?: emptySet()
    }

    /**
     * Get current user's ID
     */
    fun getCurrentUserId(): UUID? {
        val user = getCurrentUser()
        return user?.id
    }

    /**
     * Check if current user has a specific role
     */
    fun hasRole(role: UserRole): Boolean {
        val user = getCurrentUser()
        return user?.hasRole(role) ?: false
    }

    /**
     * Check if current user has any of the specified roles
     */
    fun hasAnyRole(vararg roles: UserRole): Boolean {
        val user = getCurrentUser()
        return user?.hasAnyRole(*roles) ?: false
    }

    /**
     * Check if current user has SUPER_ADMIN role
     */
    fun isSuperAdmin(): Boolean {
        return hasRole(UserRole.SUPER_ADMIN)
    }

    /**
     * Check if current user has ADMINISTRATOR role
     */
    fun isAdministrator(): Boolean {
        return hasRole(UserRole.ADMINISTRATOR)
    }

    /**
     * Check if current user has permission (from JWT claims)
     */
    fun hasPermission(permission: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }

        return try {
            val authorities = authentication.authorities
            authorities.any { authority ->
                authority.authority == permission || 
                authority.authority == "ROLE_$permission" ||
                authority.authority.contains(permission, ignoreCase = true)
            }
        } catch (e: Exception) {
            logger.warn("Failed to check permission: $permission", e)
            false
        }
    }

    /**
     * Check if current user has MANAGE_ADMINISTRATORS permission
     */
    fun canManageAdministrators(): Boolean {
        return isSuperAdmin() || hasPermission("MANAGE_ADMINISTRATORS")
    }

    /**
     * Check if the provided user ID matches the current authenticated user's ID
     * Safe check that resolves the current user entity from DB via Keycloak ID
     */
    fun isCurrentUserId(userId: UUID): Boolean {
        val currentUser = getCurrentUser() ?: return false
        return currentUser.id == userId
    }
}







