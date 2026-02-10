package com.visor.school.userservice.config;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.visor.school.userservice.service.SecurityContextService;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final SecurityContextService securityContextService;

    public CustomPermissionEvaluator(SecurityContextService securityContextService) {
        this.securityContextService = securityContextService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String permissionString = permission.toString();

        // Check MANAGE_ADMINISTRATORS permission
        if ("MANAGE_ADMINISTRATORS".equals(permissionString)) {
            return securityContextService.canManageAdministrators();
        }

        // Check if user has the permission via authorities
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String auth = authority.getAuthority();
            if (auth.equals(permissionString) ||
                auth.equals("ROLE_" + permissionString) ||
                auth.toLowerCase().contains(permissionString.toLowerCase())) {
                return true;
            }
        }

        // Check via SecurityContextService
        return securityContextService.hasPermission(permissionString);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // For now, delegate to the other hasPermission method
        return hasPermission(authentication, null, permission);
    }
}
