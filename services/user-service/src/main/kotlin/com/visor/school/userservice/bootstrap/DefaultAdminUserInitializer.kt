package com.visor.school.userservice.bootstrap

import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Initializes the default super admin user after application startup
 * This runs after Keycloak initialization is complete
 */
@Component
class DefaultAdminUserInitializer(
    private val userService: UserService,
    @Value("\${default-admin.email}") private val adminEmail: String,
    @Value("\${default-admin.password}") private val adminPassword: String,
    @Value("\${default-admin.first-name}") private val adminFirstName: String,
    @Value("\${default-admin.last-name}") private val adminLastName: String,
    @Value("\${default-admin.phone-number}") private val adminPhoneNumber: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun createDefaultAdminUser() {
        try {
            // Check if admin user already exists
            val existingUser = userService.findByEmail(adminEmail)
            if (existingUser != null) {
                log.info("Default super admin user already exists: {}", adminEmail)
                return
            }

            log.info("Creating default super admin user: {}", adminEmail)
            
            val adminUser = userService.createSystemUser(
                email = adminEmail,
                firstName = adminFirstName,
                lastName = adminLastName,
                roles = setOf(UserRole.SUPER_ADMIN),
                password = adminPassword,
                phoneNumber = adminPhoneNumber
            )

            log.info("Default super admin user created successfully with ID: {}", adminUser.id)
            log.info("Login credentials - Email: {}, Password: {}", adminEmail, adminPassword)
            
        } catch (ex: Exception) {
            log.error("Failed to create default super admin user", ex)
            // Don't fail application startup if default user creation fails
        }
    }
}
