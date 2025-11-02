package com.visor.school.security

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Utility class for JWT token operations
 */
object JwtUtil {
    
    /**
     * Get current JWT token from security context
     */
    fun getCurrentJwt(): Jwt? {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.credentials as? Jwt
    }

    /**
     * Get user ID from JWT token
     */
    fun getUserId(): String? {
        return getCurrentJwt()?.subject
    }

    /**
     * Get username from JWT token
     */
    fun getUsername(): String? {
        val jwt = getCurrentJwt() ?: return null
        @Suppress("UNCHECKED_CAST")
        return jwt.getClaim<String>("preferred_username") 
            ?: jwt.getClaim<String>("username")
    }

    /**
     * Get email from JWT token
     */
    fun getEmail(): String? {
        return getCurrentJwt()?.getClaim<String>("email")
    }

    /**
     * Get roles from JWT token
     */
    fun getRoles(): List<String> {
        val jwt = getCurrentJwt() ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        val roles = realmAccess?.get("roles") as? List<*>
        return roles?.mapNotNull { it?.toString() } ?: emptyList()
    }

    /**
     * Check if user has specific role
     */
    fun hasRole(role: String): Boolean {
        return getRoles().contains(role)
    }

    /**
     * Get claim value from JWT
     */
    fun <T> getClaim(claimName: String, type: Class<T>): T? {
        return getCurrentJwt()?.getClaim(claimName)
    }
}

