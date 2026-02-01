package com.visor.school.userservice.service

import com.visor.school.userservice.dto.LoginResponse
import com.visor.school.userservice.event.UserEventPublisher
import com.visor.school.userservice.integration.KeycloakClient
import com.visor.school.userservice.integration.KeycloakException
import com.visor.school.userservice.integration.UserAlreadyExistsException
import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import com.visor.school.userservice.repository.UserRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
     * Create a new user with multiple roles
     * Flow: 1. Create user in Keycloak, 2. Get keycloakId, 3. Create User entity
     * Authorization: SUPER_ADMIN can create ADMINISTRATOR users; ADMINISTRATOR cannot create ADMINISTRATOR or SUPER_ADMIN users
     */
    fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        roles: Set<UserRole>,
        password: String,
        phoneNumber: String? = null
    ): User {
        if (roles.isEmpty()) {
            throw IllegalArgumentException("User must have at least one role")
        }

        logger.info("Creating user: $email with roles: $roles")

        // Authorization check: Only SUPER_ADMIN can create ADMINISTRATOR or SUPER_ADMIN users
        if (roles.contains(UserRole.ADMINISTRATOR) || roles.contains(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can create users with ADMINISTRATOR or SUPER_ADMIN roles")
            }
        }

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("User with email $email already exists")
        }

        try {
            // Step 1: Create user in Keycloak first with attributes
            val keycloakId = keycloakClient.createUser(
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = password,
                emailVerified = false,
                attributes = mapOf(
                    "firstLogin" to "true"
                )
            )

            // Step 2: Assign roles in Keycloak (using admin client for permissions)
            val roleNames = roles.map { it.name }.toSet()
            keycloakClient.assignRealmRolesAsAdmin(keycloakId, roleNames)
            logger.info("Roles ${roleNames} assigned to Keycloak user: $keycloakId")

            // Step 3: Create User entity with keycloakId
            val user = User(
                keycloakId = keycloakId,
                email = email,
                roles = roles.toMutableSet(),
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                emailVerified = false,
                accountStatus = AccountStatus.PENDING
            )

            val saved = userRepository.save(user)
            logger.info("User created successfully: ${saved.id} (Keycloak ID: $keycloakId)")

            // Publish user created event
            eventPublisher.publishUserCreated(saved)

            return saved
        } catch (e: UserAlreadyExistsException) {
            logger.error("User already exists in Keycloak: $email", e)
            throw IllegalArgumentException("User with email $email already exists!", e)
        } catch (e: KeycloakException) {
            logger.error("Keycloak error while creating user: $email", e)
            throw RuntimeException("Failed to create user: ${e.message}", e)
        }
    }

    /**
     * Create a user during system initialization with multiple roles (bypasses security checks)
     * This should only be used by bootstrap/initialization components
     * If user already exists in Keycloak, syncs it to local database
     */
    fun createSystemUser(
        email: String,
        firstName: String,
        lastName: String,
        roles: Set<UserRole>,
        password: String,
        phoneNumber: String? = null
    ): User {
        if (roles.isEmpty()) {
            throw IllegalArgumentException("User must have at least one role")
        }

        logger.info("Creating system user: $email with roles: $roles")

        // Check if user already exists in local database
        val existingUser = userRepository.findByEmail(email).orElse(null)
        if (existingUser != null) {
            logger.info("User already exists in local database: $email")
            return existingUser
        }

        try {
            // Step 1: Create user in Keycloak first (using admin credentials)
            val keycloakId = keycloakClient.createUserAsAdmin(
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = password,
                emailVerified = true,
                attributes = mapOf(
                    "firstLogin" to "true"
                )
            )

            // Step 2: Assign roles in Keycloak (using admin credentials)
            val roleNames = roles.map { it.name }.toSet()
            keycloakClient.assignRealmRolesAsAdmin(keycloakId, roleNames)

            // Step 3: Create User entity with keycloakId
            val user = User(
                keycloakId = keycloakId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                roles = roles.toMutableSet(),
                phoneNumber = phoneNumber,
                emailVerified = true,
                accountStatus = AccountStatus.ACTIVE
            )

            val saved = userRepository.save(user)
            logger.info("System user created successfully: ${saved.id} (Keycloak ID: $keycloakId)")

            // Publish user created event
            eventPublisher.publishUserCreated(saved)

            return saved
        } catch (e: UserAlreadyExistsException) {
            // User exists in Keycloak but not in local DB - need to sync
            logger.warn("User exists in Keycloak but not in local database, syncing: $email")
            
            try {
                // Get user from Keycloak to get the keycloakId
                val keycloakUser = keycloakClient.getUserByEmail(email)
                    ?: throw IllegalStateException("User exists in Keycloak but could not retrieve details for: $email")
                
                // Update user details in Keycloak
                keycloakUser.firstName = firstName
                keycloakUser.lastName = lastName
                keycloakUser.isEmailVerified = true
                keycloakClient.updateUser(keycloakUser.id, keycloakUser)
                
                // Sync roles in Keycloak
                val roleNames = roles.map { it.name }.toSet()
                keycloakClient.syncRealmRoles(keycloakUser.id, roleNames)
                
                // Create local User entity
                val user = User(
                    keycloakId = keycloakUser.id,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    roles = roles.toMutableSet(),
                    phoneNumber = phoneNumber,
                    emailVerified = true,
                    accountStatus = AccountStatus.ACTIVE
                )

                val saved = userRepository.save(user)
                logger.info("System user synced from Keycloak: ${saved.id} (Keycloak ID: ${keycloakUser.id})")

                // Publish user created event
                eventPublisher.publishUserCreated(saved)

                return saved
            } catch (syncError: Exception) {
                logger.error("Failed to sync user from Keycloak: $email", syncError)
                throw IllegalArgumentException("User exists in Keycloak but failed to sync to local database: ${syncError.message}", syncError)
            }
        } catch (e: KeycloakException) {
            logger.error("Keycloak error while creating system user: $email", e)
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
     * Update user information with multiple roles
     * Authorization: SUPER_ADMIN can update ADMINISTRATOR users; ADMINISTRATOR cannot update ADMINISTRATOR or SUPER_ADMIN users
     */
    fun updateUser(
        id: UUID,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        roles: Set<UserRole>? = null
    ): User {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found: $id") }

        // Authorization check: Only SUPER_ADMIN can update ADMINISTRATOR or SUPER_ADMIN users
        if (user.hasRole(UserRole.ADMINISTRATOR) || user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update users with ADMINISTRATOR or SUPER_ADMIN roles")
            }
        }

        // Prevent role escalation: ADMINISTRATOR cannot add SUPER_ADMIN role
        if (roles != null && roles.contains(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.hasRole(UserRole.SUPER_ADMIN)) {
                throw SecurityException("Only SUPER_ADMIN can assign SUPER_ADMIN role")
            }
        }

        // Prevent ADMINISTRATOR from modifying SUPER_ADMIN users
        if (user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.hasRole(UserRole.SUPER_ADMIN)) {
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

        if (roles != null) {
            if (roles.isEmpty()) {
                throw IllegalArgumentException("User must have at least one role")
            }
            
            // Sync roles with Keycloak
            val roleNames = roles.map { it.name }.toSet()
            keycloakClient.syncRealmRoles(user.keycloakId, roleNames)
            
            // Update local roles
            user.replaceRoles(roles)
            logger.info("Updating user $id: roles to $roles")
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
        if (user.hasRole(UserRole.ADMINISTRATOR) || user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update users with ADMINISTRATOR or SUPER_ADMIN roles account status")
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
        if (targetUser.hasRole(UserRole.ADMINISTRATOR) || targetUser.hasRole(UserRole.SUPER_ADMIN)) {
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
     * Update last login timestamp and mark firstLogin as false if it was true
     */
    fun updateLastLogin(id: UUID) {
        val user = userRepository.findById(id).orElse(null)
        user?.let {
            val wasFirstLogin = it.keycloakId?.let { keycloakId ->
                val keycloakUser = keycloakClient.getUser(keycloakId)
                keycloakUser?.attributes?.get("firstLogin")?.firstOrNull() == "true"
            } ?: false
            
            it.updateLastLogin()
            userRepository.save(it)
            
            // Update firstLogin attribute in Keycloak if this was the first login
            if (wasFirstLogin) {
                it.keycloakId?.let { keycloakId ->
                    val keycloakUser = keycloakClient.getUser(keycloakId)
                    if (keycloakUser != null) {
                        keycloakUser.attributes = (keycloakUser.attributes ?: mutableMapOf()).apply {
                            put("firstLogin", listOf("false"))
                        }
                        keycloakClient.updateUser(keycloakId, keycloakUser)
                        logger.info("Updated firstLogin attribute to false for user: $id")
                    }
                }
            }
        }
    }

    /**
     * Authenticate user with Keycloak
     * @return LoginResponse with access token and refresh token
     */
    fun authenticateUser(email: String, password: String): LoginResponse {
        return keycloakClient.authenticateUser(email, password)
    }

    fun refreshToken(refreshToken: String): LoginResponse {
        return keycloakClient.refreshToken(refreshToken)
    }

    fun getMe(): User? {
        return securityContextService.getCurrentUser()
    }
}
