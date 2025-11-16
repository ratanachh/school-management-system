package com.visor.school.userservice.service

import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.integration.KeycloakException
import com.visor.school.userservice.integration.UserAlreadyExistsException
import com.visor.school.userservice.event.UserEventPublisher
import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * User service with Keycloak integration
 * User creation flow: Create Keycloak user first → receive keycloakId → create User entity
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val keycloakClient: KeycloakClient,
    private val eventPublisher: UserEventPublisher,
    private val securityContextService: SecurityContextService
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Create a new user
     * Flow: 1. Create user in Keycloak, 2. Get keycloakId, 3. Create User entity
     * Authorization: SUPER_ADMIN can create ADMINISTRATOR users; ADMINISTRATOR cannot create ADMINISTRATOR or SUPER_ADMIN users
     */
    fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        password: String,
        phoneNumber: String? = null
    ): User {
        logger.info("Creating user: $email with role: $role")

        // Authorization check: Only SUPER_ADMIN can create ADMINISTRATOR or SUPER_ADMIN users
        if (role == UserRole.ADMINISTRATOR || role == UserRole.SUPER_ADMIN) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can create $role users")
            }
        }

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("User with email $email already exists")
        }

        try {
            // Step 1: Create user in Keycloak first
            val keycloakId = keycloakClient.createUser(
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = password,
                emailVerified = false
            )

            // Step 2: Create User entity with keycloakId
            val user = User(
                keycloakId = keycloakId,
                email = email,
                role = role,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                emailVerified = false,
                accountStatus = AccountStatus.ACTIVE
            )

            val saved = userRepository.save(user)
            logger.info("User created successfully: ${saved.id} (Keycloak ID: $keycloakId)")

            // Publish user created event
            eventPublisher.publishUserCreated(saved)

            return saved
        } catch (e: UserAlreadyExistsException) {
            logger.error("User already exists in Keycloak: $email", e)
            throw IllegalArgumentException("User with email $email already exists in Keycloak", e)
        } catch (e: KeycloakException) {
            logger.error("Keycloak error while creating user: $email", e)
            throw RuntimeException("Failed to create user in Keycloak: ${e.message}", e)
        }
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    fun findById(id: UUID): User? {
        return userRepository.findById(id).orElse(null)
    }

    /**
     * Find user by Keycloak ID
     */
    @Transactional(readOnly = true)
    fun findByKeycloakId(keycloakId: String): User? {
        return userRepository.findByKeycloakId(keycloakId).orElse(null)
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }

    /**
     * Update user information
     * Authorization: SUPER_ADMIN can update ADMINISTRATOR users; ADMINISTRATOR cannot update ADMINISTRATOR or SUPER_ADMIN users
     */
    fun updateUser(
        id: UUID,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        role: UserRole? = null
    ): User {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        // Authorization check: Only SUPER_ADMIN can update ADMINISTRATOR or SUPER_ADMIN users
        if (user.role == UserRole.ADMINISTRATOR || user.role == UserRole.SUPER_ADMIN) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update ${user.role} users")
            }
        }

        // Prevent role escalation: ADMINISTRATOR cannot change role to SUPER_ADMIN
        if (role != null && role == UserRole.SUPER_ADMIN) {
            val currentRole = securityContextService.getCurrentUserRole()
            if (currentRole != UserRole.SUPER_ADMIN) {
                throw SecurityException("Only SUPER_ADMIN can assign SUPER_ADMIN role")
            }
        }

        // Prevent ADMINISTRATOR from modifying SUPER_ADMIN users
        if (user.role == UserRole.SUPER_ADMIN) {
            val currentRole = securityContextService.getCurrentUserRole()
            if (currentRole != UserRole.SUPER_ADMIN) {
                throw SecurityException("Only SUPER_ADMIN can modify SUPER_ADMIN users")
            }
        }

        if (firstName != null) {
            user.firstName = firstName
            logger.info("Updating user $id: firstName")
        }

        if (lastName != null) {
            user.lastName = lastName
            logger.info("Updating user $id: lastName")
        }

        if (phoneNumber != null) {
            user.phoneNumber = phoneNumber
            logger.info("Updating user $id: phoneNumber")
        }

        if (role != null) {
            user.role = role
            logger.info("Updating user $id: role to $role")
        }

        user.updatedAt = java.time.Instant.now()

        return userRepository.save(user)
    }

    /**
     * Update account status
     * Authorization: SUPER_ADMIN can update ADMINISTRATOR account status; ADMINISTRATOR cannot update ADMINISTRATOR or SUPER_ADMIN account status
     */
    fun updateAccountStatus(id: UUID, status: AccountStatus): User {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        // Authorization check: Only SUPER_ADMIN can update ADMINISTRATOR or SUPER_ADMIN account status
        if (user.role == UserRole.ADMINISTRATOR || user.role == UserRole.SUPER_ADMIN) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update ${user.role} account status")
            }
        }

        user.updateStatus(status)
        logger.info("Updated account status for user $id to $status")

        return userRepository.save(user)
    }

    /**
     * Check if current user has permission to manage another user based on roles
     * Returns true if:
     * - Current user is SUPER_ADMIN (can manage anyone)
     * - Current user has MANAGE_ADMINISTRATORS permission (can manage ADMINISTRATOR)
     * - Target user is not ADMINISTRATOR or SUPER_ADMIN (regular users can be managed by ADMINISTRATOR)
     */
    fun canManageUser(targetUserId: UUID): Boolean {
        val targetUser = userRepository.findById(targetUserId).orElse(null)
            ?: return false

        // SUPER_ADMIN can manage anyone
        if (securityContextService.isSuperAdmin()) {
            return true
        }

        // If target is ADMINISTRATOR or SUPER_ADMIN, need MANAGE_ADMINISTRATORS permission
        if (targetUser.role == UserRole.ADMINISTRATOR || targetUser.role == UserRole.SUPER_ADMIN) {
            return securityContextService.canManageAdministrators()
        }

        // ADMINISTRATOR can manage regular users (TEACHER, STUDENT, PARENT)
        return securityContextService.isAdministrator()
    }

    /**
     * Verify user email
     */
    fun verifyEmail(id: UUID): User {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        user.verifyEmail()
        
        // Update Keycloak email verification status
        keycloakClient.updateEmailVerification(user.keycloakId, true)

        logger.info("Email verified for user: $id")
        val saved = userRepository.save(user)
        
        // Publish email verified event
        eventPublisher.publishEmailVerified(saved)
        
        return saved
    }

    /**
     * Update last login timestamp
     */
    fun updateLastLogin(id: UUID) {
        val user = userRepository.findById(id).orElse(null)
        user?.let {
            it.updateLastLogin()
            userRepository.save(it)
        }
    }
}

