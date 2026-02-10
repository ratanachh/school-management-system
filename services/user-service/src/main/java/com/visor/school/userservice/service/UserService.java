package com.visor.school.userservice.service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.dto.LoginResponse;
import com.visor.school.userservice.event.UserEventPublisher;
import com.visor.school.userservice.integration.KeycloakClient;
import com.visor.school.userservice.integration.KeycloakException;
import com.visor.school.userservice.integration.UserAlreadyExistsException;
import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;

/**
 * User service with Keycloak integration
 * User creation flow: Create Keycloak user first -> receive keycloakId -> create User entity
 */
@Service
@Transactional
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    private final UserEventPublisher eventPublisher;
    private final SecurityContextService securityContextService;

    public UserService(
        UserRepository userRepository,
        KeycloakClient keycloakClient,
        UserEventPublisher eventPublisher,
        SecurityContextService securityContextService
    ) {
        this.userRepository = userRepository;
        this.keycloakClient = keycloakClient;
        this.eventPublisher = eventPublisher;
        this.securityContextService = securityContextService;
    }

    /**
     * Create a new user with multiple roles
     * Flow: 1. Create user in Keycloak, 2. Get keycloakId, 3. Create User entity
     * Authorization: SUPER_ADMIN can create ADMINISTRATOR users; ADMINISTRATOR cannot create ADMINISTRATOR or SUPER_ADMIN users
     */
    public User createUser(
        String email,
        String firstName,
        String lastName,
        Set<UserRole> roles,
        String password,
        String phoneNumber
    ) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        logger.info("Creating user: {} with roles: {}", email, roles);

        // Authorization check: Only SUPER_ADMIN can create ADMINISTRATOR or SUPER_ADMIN users
        if (roles.contains(UserRole.ADMINISTRATOR) || roles.contains(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw new SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can create users with ADMINISTRATOR or SUPER_ADMIN roles");
            }
        }

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        try {
            // Step 1: Create user in Keycloak first with attributes
            Map<String, String> attributes = new HashMap<>();
            attributes.put("firstLogin", "true");

            String keycloakId = keycloakClient.createUser(
                email,
                firstName,
                lastName,
                password,
                false,
                attributes
            );

            // Step 2: Assign roles in Keycloak (using admin client for permissions)
            Set<String> roleNames = roles.stream().map(UserRole::name).collect(Collectors.toSet());
            keycloakClient.assignRealmRolesAsAdmin(keycloakId, roleNames);
            logger.info("Roles {} assigned to Keycloak user: {}", roleNames, keycloakId);

            // Step 3: Create User entity with keycloakId
            User user = new User(
                keycloakId,
                email,
                firstName,
                lastName,
                new HashSet<>(roles),
                phoneNumber,
                false,
                AccountStatus.PENDING
            );

            User saved = userRepository.save(user);
            logger.info("User created successfully: {} (Keycloak ID: {})", saved.getId(), keycloakId);

            // Publish user created event
            eventPublisher.publishUserCreated(saved);

            return saved;
        } catch (UserAlreadyExistsException e) {
            logger.error("User already exists in Keycloak: {}", email, e);
            throw new IllegalArgumentException("User with email " + email + " already exists!", e);
        } catch (KeycloakException e) {
            logger.error("Keycloak error while creating user: {}", email, e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Create a user during system initialization with multiple roles (bypasses security checks)
     * This should only be used by bootstrap/initialization components
     * If user already exists in Keycloak, syncs it to local database
     */
    public User createSystemUser(
        String email,
        String firstName,
        String lastName,
        Set<UserRole> roles,
        String password,
        String phoneNumber
    ) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        logger.info("Creating system user: {} with roles: {}", email, roles);

        // Check if user already exists in local database
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            logger.info("User already exists in local database: {}", email);
            return existingUser;
        }

        try {
            // Step 1: Create user in Keycloak first (using admin credentials)
            Map<String, String> attributes = new HashMap<>();
            attributes.put("firstLogin", "true");

            String keycloakId = keycloakClient.createUserAsAdmin(
                email,
                firstName,
                lastName,
                password,
                true,
                attributes
            );

            // Step 2: Assign roles in Keycloak (using admin credentials)
            Set<String> roleNames = roles.stream().map(UserRole::name).collect(Collectors.toSet());
            keycloakClient.assignRealmRolesAsAdmin(keycloakId, roleNames);

            // Step 3: Create User entity with keycloakId
            User user = new User(
                keycloakId,
                email,
                firstName,
                lastName,
                new HashSet<>(roles),
                phoneNumber,
                true,
                AccountStatus.ACTIVE
            );

            User saved = userRepository.save(user);
            logger.info("System user created successfully: {} (Keycloak ID: {})", saved.getId(), keycloakId);

            // Publish user created event
            eventPublisher.publishUserCreated(saved);

            return saved;
        } catch (UserAlreadyExistsException e) {
            // User exists in Keycloak but not in local DB - need to sync
            logger.warn("User exists in Keycloak but not in local database, syncing: {}", email);
            
            try {
                // Get user from Keycloak to get the keycloakId
                UserRepresentation keycloakUser = keycloakClient.getUserByEmail(email);
                if (keycloakUser == null) {
                    throw new IllegalStateException("User exists in Keycloak but could not retrieve details for: " + email);
                }
                
                // Update user details in Keycloak
                keycloakUser.setFirstName(firstName);
                keycloakUser.setLastName(lastName);
                keycloakUser.setEmailVerified(true);
                keycloakClient.updateUser(keycloakUser.getId(), keycloakUser);
                
                // Sync roles in Keycloak
                Set<String> roleNames = roles.stream().map(UserRole::name).collect(Collectors.toSet());
                keycloakClient.syncRealmRoles(keycloakUser.getId(), roleNames);
                
                // Create local User entity
                User user = new User(
                    keycloakUser.getId(),
                    email,
                    firstName,
                    lastName,
                    new HashSet<>(roles),
                    phoneNumber,
                    true,
                    AccountStatus.ACTIVE
                );

                User saved = userRepository.save(user);
                logger.info("System user synced from Keycloak: {} (Keycloak ID: {})", saved.getId(), keycloakUser.getId());

                // Publish user created event
                eventPublisher.publishUserCreated(saved);

                return saved;
            } catch (Exception syncError) {
                logger.error("Failed to sync user from Keycloak: {}", email, syncError);
                throw new IllegalArgumentException("User exists in Keycloak but failed to sync to local database: " + syncError.getMessage(), syncError);
            }
        } catch (KeycloakException e) {
            logger.error("Keycloak error while creating system user: {}", email, e);
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public User findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId).orElse(null);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Update user information with multiple roles
     */
    public User updateUser(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        Set<UserRole> roles
    ) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Authorization check: Only SUPER_ADMIN can update ADMINISTRATOR or SUPER_ADMIN users
        if (user.hasRole(UserRole.ADMINISTRATOR) || user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw new SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update users with ADMINISTRATOR or SUPER_ADMIN roles");
            }
        }

        // Prevent role escalation: ADMINISTRATOR cannot add SUPER_ADMIN role
        if (roles != null && roles.contains(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.hasRole(UserRole.SUPER_ADMIN)) {
                throw new SecurityException("Only SUPER_ADMIN can assign SUPER_ADMIN role");
            }
        }

        // Prevent ADMINISTRATOR from modifying SUPER_ADMIN users
        if (user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.hasRole(UserRole.SUPER_ADMIN)) {
                throw new SecurityException("Only SUPER_ADMIN can modify SUPER_ADMIN users");
            }
        }

        if (firstName != null) {
            user.setFirstName(firstName);
            logger.info("Updating user {}: firstName", id);
        }

        if (lastName != null) {
            user.setLastName(lastName);
            logger.info("Updating user {}: lastName", id);
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
            logger.info("Updating user {}: phoneNumber", id);
        }

        if (roles != null) {
            if (roles.isEmpty()) {
                throw new IllegalArgumentException("User must have at least one role");
            }
            
            // Sync roles with Keycloak
            Set<String> roleNames = roles.stream().map(UserRole::name).collect(Collectors.toSet());
            keycloakClient.syncRealmRoles(user.getKeycloakId(), roleNames);
            
            // Update local roles
            user.replaceRoles(roles);
            logger.info("Updating user {}: roles to {}", id, roles);
        }

        user.setUpdatedAt(Instant.now());

        return userRepository.save(user);
    }

    /**
     * Update account status
     */
    public User updateAccountStatus(UUID id, AccountStatus status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Authorization check: Only SUPER_ADMIN can update ADMINISTRATOR or SUPER_ADMIN account status
        if (user.hasRole(UserRole.ADMINISTRATOR) || user.hasRole(UserRole.SUPER_ADMIN)) {
            if (!securityContextService.canManageAdministrators()) {
                throw new SecurityException("Only SUPER_ADMIN or users with MANAGE_ADMINISTRATORS permission can update users with ADMINISTRATOR or SUPER_ADMIN roles account status");
            }
        }

        user.updateStatus(status);
        logger.info("Updated account status for user {} to {}", id, status);

        return userRepository.save(user);
    }

    public boolean canManageUser(UUID targetUserId) {
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        if (targetUser == null) return false;

        // SUPER_ADMIN can manage anyone
        if (securityContextService.isSuperAdmin()) {
            return true;
        }

        // If target is ADMINISTRATOR or SUPER_ADMIN, need MANAGE_ADMINISTRATORS permission
        if (targetUser.hasRole(UserRole.ADMINISTRATOR) || targetUser.hasRole(UserRole.SUPER_ADMIN)) {
            return securityContextService.canManageAdministrators();
        }

        // ADMINISTRATOR can manage regular users (TEACHER, STUDENT, PARENT)
        return securityContextService.isAdministrator();
    }

    public User verifyEmail(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        user.verifyEmail();
        
        // Update Keycloak email verification status
        keycloakClient.updateEmailVerification(user.getKeycloakId(), true);

        logger.info("Email verified for user: {}", id);
        User saved = userRepository.save(user);
        
        // Publish email verified event
        eventPublisher.publishEmailVerified(saved);
        
        return saved;
    }

    @Transactional
    public void updateLastLogin(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            boolean wasFirstLogin = false;
            String keycloakId = user.getKeycloakId();
            if (keycloakId != null) {
                UserRepresentation keycloakUser = keycloakClient.getUser(keycloakId);
                // Check attribute firstLogin == "true"
                if (keycloakUser != null && keycloakUser.getAttributes() != null) {
                    List<String> firstLoginAttr = keycloakUser.getAttributes().get("firstLogin");
                    if (firstLoginAttr != null && !firstLoginAttr.isEmpty() && "true".equals(firstLoginAttr.get(0))) {
                        wasFirstLogin = true;
                    }
                }
            }
            
            user.updateLastLogin();
            userRepository.save(user);
            
            // Update firstLogin attribute in Keycloak if this was the first login
            if (wasFirstLogin) {
                UserRepresentation keycloakUser = keycloakClient.getUser(keycloakId);
                if (keycloakUser != null) {
                    Map<String, List<String>> attributes = keycloakUser.getAttributes();
                    if (attributes == null) {
                        attributes = new HashMap<>(); // make copy or new
                    } else {
                        attributes = new HashMap<>(attributes); // ensure mutable
                    }
                    attributes.put("firstLogin", Collections.singletonList("false"));
                    keycloakUser.setAttributes(attributes);
                    keycloakClient.updateUser(keycloakId, keycloakUser);
                    logger.info("Updated firstLogin attribute to false for user: {}", id);
                }
            }
        }
    }

    public LoginResponse authenticateUser(String email, String password) {
        return keycloakClient.authenticateUser(email, password);
    }

    public LoginResponse refreshToken(String refreshToken) {
        return keycloakClient.refreshToken(refreshToken);
    }

    public User getMe() {
        return securityContextService.getCurrentUser();
    }
}
