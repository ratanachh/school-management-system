package com.visor.school.userservice.bootstrap;

import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initializes the default super admin user after application startup
 * This runs after Keycloak initialization is complete
 */
@Component
public class DefaultAdminUserInitializer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFirstName;
    private final String adminLastName;
    private final String adminPhoneNumber;

    public DefaultAdminUserInitializer(
        UserService userService,
        @Value("${default-admin.email}") String adminEmail,
        @Value("${default-admin.password}") String adminPassword,
        @Value("${default-admin.first-name}") String adminFirstName,
        @Value("${default-admin.last-name}") String adminLastName,
        @Value("${default-admin.phone-number}") String adminPhoneNumber
    ) {
        this.userService = userService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminPhoneNumber = adminPhoneNumber;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2) // Run after KeycloakBootstrapInitializer
    public void createDefaultAdminUser() {
        try {
            // Check if admin user already exists
            if (userService.findByEmail(adminEmail) != null) {
                log.info("Default super admin user already exists: {}", adminEmail);
                return;
            }

            log.info("Creating default super admin user: {}", adminEmail);

            var adminUser = userService.createSystemUser(
                adminEmail,
                adminFirstName,
                adminLastName,
                Set.of(UserRole.SUPER_ADMIN),
                adminPassword,
                adminPhoneNumber
            );

            log.info("Default super admin user created successfully with ID: {}", adminUser.getId());
            log.info("Login credentials - Email: {}, Password: {}", adminEmail, adminPassword);

        } catch (Exception ex) {
            log.error("Failed to create default super admin user", ex);
            // Don't fail application startup if default user creation fails
        }
    }
}
