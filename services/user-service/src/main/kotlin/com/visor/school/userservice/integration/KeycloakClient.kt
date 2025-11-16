package com.visor.school.userservice.integration

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import jakarta.ws.rs.core.Response

/**
 * Keycloak Admin API client for user management
 * Handles user creation, password reset, and user management operations
 */
@Component
class KeycloakClient(
    @Value("\${keycloak.server-url}") private val serverUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.admin-client-id}") private val adminClientId: String,
    @Value("\${keycloak.admin-client-secret}") private val adminClientSecret: String
) {
    private val logger = LoggerFactory.getLogger(KeycloakClient::class.java)

    private val keycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .clientId(adminClientId)
            .clientSecret(adminClientSecret)
            .grantType("client_credentials")
            .build()
    }

    /**
     * Create a new user in Keycloak
     * @return Keycloak user ID (UUID as string)
     */
    fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        emailVerified: Boolean = false
    ): String {
        logger.info("Creating user in Keycloak: $email")

        val userRepresentation = UserRepresentation().apply {
            this.email = email
            this.firstName = firstName
            this.lastName = lastName
            this.isEnabled = true
            this.isEmailVerified = emailVerified
            this.username = email
        }

        val response = keycloak.realm(realm).users().create(userRepresentation)

        return when (response.status) {
            201 -> {
                val userId = extractUserIdFromLocation(response.location?.toString())
                logger.info("User created in Keycloak with ID: $userId")
                
                // Set password
                setPassword(userId, password, false)
                
                userId
            }
            409 -> throw UserAlreadyExistsException("User with email $email already exists in Keycloak")
            else -> throw KeycloakException("Failed to create user in Keycloak: ${response.statusInfo.reasonPhrase}")
        }
    }

    /**
     * Reset user password in Keycloak
     */
    fun resetPassword(keycloakId: String, newPassword: String, temporary: Boolean = true) {
        logger.info("Resetting password for Keycloak user: $keycloakId")
        setPassword(keycloakId, newPassword, temporary)
    }

    /**
     * Set password for a user
     */
    private fun setPassword(keycloakId: String, password: String, temporary: Boolean) {
        val credential = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = password
            isTemporary = temporary
        }

        keycloak.realm(realm).users().get(keycloakId).resetPassword(credential)
        logger.info("Password set for Keycloak user: $keycloakId (temporary: $temporary)")
    }

    /**
     * Get user by Keycloak ID
     */
    fun getUser(keycloakId: String): UserRepresentation? {
        return try {
            keycloak.realm(realm).users().get(keycloakId).toRepresentation()
        } catch (e: Exception) {
            logger.warn("User not found in Keycloak: $keycloakId", e)
            null
        }
    }

    /**
     * Update user in Keycloak
     */
    fun updateUser(keycloakId: String, userRepresentation: UserRepresentation) {
        logger.info("Updating user in Keycloak: $keycloakId")
        try {
            keycloak.realm(realm).users().get(keycloakId).update(userRepresentation)
            logger.info("User updated in Keycloak: $keycloakId")
        } catch (e: Exception) {
            logger.error("Failed to update user in Keycloak: $keycloakId", e)
            throw KeycloakException("Failed to update user in Keycloak: ${e.message}", e)
        }
    }

    /**
     * Update user email verification status
     */
    fun updateEmailVerification(keycloakId: String, verified: Boolean) {
        logger.info("Updating email verification for Keycloak user: $keycloakId to $verified")
        val user = keycloak.realm(realm).users().get(keycloakId)
        val representation = user.toRepresentation()
        representation.isEmailVerified = verified
        user.update(representation)
    }

    /**
     * Extract user ID from Keycloak response location header
     */
    private fun extractUserIdFromLocation(location: String?): String {
        if (location == null) {
            throw KeycloakException("Location header is null")
        }
        return location.substringAfterLast("/")
    }
}

class KeycloakException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class UserAlreadyExistsException(message: String) : RuntimeException(message)

