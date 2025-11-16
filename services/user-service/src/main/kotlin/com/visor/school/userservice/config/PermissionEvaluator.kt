package com.visor.school.userservice.config

import com.visor.school.userservice.service.SecurityContextService
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

/**
 * Custom permission evaluator for fine-grained access control
 * Supports MANAGE_ADMINISTRATORS permission and other custom permissions
 */
@Component
class CustomPermissionEvaluator(
    private val securityContextService: SecurityContextService
) : PermissionEvaluator {

    override fun hasPermission(
        authentication: Authentication?,
        targetDomainObject: Any?,
        permission: Any
    ): Boolean {
        if (authentication == null || !authentication.isAuthenticated) {
            return false
        }

        val permissionString = permission.toString()

        // Check MANAGE_ADMINISTRATORS permission
        if (permissionString == "MANAGE_ADMINISTRATORS") {
            return securityContextService.canManageAdministrators()
        }

        // Check if user has the permission via authorities
        val hasAuthority = authentication.authorities.any { authority ->
            authority.authority == permissionString ||
            authority.authority == "ROLE_$permissionString" ||
            authority.authority.contains(permissionString, ignoreCase = true)
        }

        if (hasAuthority) {
            return true
        }

        // Check via SecurityContextService
        return securityContextService.hasPermission(permissionString)
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable?,
        targetType: String?,
        permission: Any
    ): Boolean {
        // For now, delegate to the other hasPermission method
        // In the future, could add target-specific permission checks
        return hasPermission(authentication, null, permission)
    }
}







