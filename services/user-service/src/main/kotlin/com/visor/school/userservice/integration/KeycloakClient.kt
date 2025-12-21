package com.visor.school.userservice.integration

import com.visor.school.userservice.dto.LoginResponse
import jakarta.ws.rs.core.Response
import java.util.UUID
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * Keycloak Admin API client for user management
 * Handles user creation, password reset, and user management operations
 */
@Component
class KeycloakClient(
    @Value("\${keycloak.server-url}") private val serverUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.service-client-id}") private val serviceClientId: String,
    @Value("\${keycloak.service-client-secret}") private val serviceClientSecret: String,
    @Value("\${keycloak.admin-username}") private val adminUsername: String,
    @Value("\${keycloak.admin-password}") private val adminPassword: String,
    @Value("\${keycloak.admin-client-id}") private val adminClientId: String
) {
    private val logger = LoggerFactory.getLogger(KeycloakClient::class.java)

    // Service account client for regular operations
    private val keycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .clientId(serviceClientId)
            .clientSecret(serviceClientSecret)
            .grantType("client_credentials")
            .build()
    }

    // Admin client for system initialization (e.g., creating initial admin user)
    private val adminKeycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master") // Admin credentials authenticate against master realm
            .clientId(adminClientId)
            .username(adminUsername)
            .password(adminPassword)
            .grantType("password")
            .build()
    }

    /**
     * Create a new user in Keycloak (using service account)
     * @param attributes Optional map of user attributes. Values are converted to lists as Keycloak expects Map<String, List<String>>
     * @return Keycloak user ID (UUID as string)
     */
    fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        emailVerified: Boolean = false,
        attributes: Map<String, String> = emptyMap()
    ): String {
        return createUserInternal(
            keycloakClient = keycloak,
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password,
            emailVerified = emailVerified,
            attributes = attributes
        )
    }

    /**
     * Create a new user in Keycloak using admin credentials
     * This should only be used for system initialization (e.g., creating first admin user)
     * @param attributes Optional map of user attributes. Values are converted to lists as Keycloak expects Map<String, List<String>>
     * @return Keycloak user ID (UUID as string)
     */
    fun createUserAsAdmin(
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        emailVerified: Boolean = false,
        attributes: Map<String, String> = emptyMap()
    ): String {
        return createUserInternal(
            keycloakClient = adminKeycloak,
            email = email,
            firstName = firstName,
            lastName = lastName,
            password = password,
            emailVerified = emailVerified,
            attributes = attributes
        )
    }

    /**
     * Internal method to create user with specified Keycloak client
     * @param attributes Optional map of user attributes. Values are converted to lists as Keycloak expects Map<String, List<String>>
     */
    private fun createUserInternal(
        keycloakClient: Keycloak,
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        emailVerified: Boolean,
        attributes: Map<String, String> = emptyMap()
    ): String {
        logger.info("Creating user in Keycloak: $email")

        val userRepresentation = UserRepresentation().apply {
            this.email = email
            this.firstName = firstName
            this.lastName = lastName
            this.isEnabled = true
            this.isEmailVerified = emailVerified
            this.username = email
            
            // Set user attributes if provided
            // Keycloak expects Map<String, List<String>> for attributes
            if (attributes.isNotEmpty()) {
                this.attributes = attributes.mapValues { listOf(it.value) }.toMutableMap()
                logger.info("Setting user attributes: \${attributes.keys}")
            }
        }

        val response = keycloakClient.realm(realm).users().create(userRepresentation)

        return when (response.status) {
            201 -> {
                val userId = extractUserIdFromLocation(response.location?.toString())
                logger.info("User created in Keycloak with ID: $userId")
                
                // Set password
                setPasswordInternal(keycloakClient, userId, password, false)
                
                userId
            }
            409 -> throw UserAlreadyExistsException("User with email $email already exists in Keycloak")
            else -> throw KeycloakException("Failed to create user in Keycloak: \${response.statusInfo.reasonPhrase}")
        }
    }

    /**
     * Reset user password in Keycloak
     */
    fun resetPassword(keycloakId: String, newPassword: String, temporary: Boolean = true) {
        logger.info("Resetting password for Keycloak user: $keycloakId")
        setPasswordInternal(keycloak, keycloakId, newPassword, temporary)
    }

    /**
     * Set password for a user (using service account)
     */
    private fun setPassword(keycloakId: String, password: String, temporary: Boolean) {
        setPasswordInternal(keycloak, keycloakId, password, temporary)
    }

    /**
     * Set password for a user with specified Keycloak client
     */
    private fun setPasswordInternal(keycloakClient: Keycloak, keycloakId: String, password: String, temporary: Boolean) {
        val credential = CredentialRepresentation().apply {
            type = CredentialRepresentation.PASSWORD
            value = password
            isTemporary = temporary
        }

        keycloakClient.realm(realm).users().get(keycloakId).resetPassword(credential)
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
     * Get user by email (using admin client)
     */
    fun getUserByEmail(email: String): UserRepresentation? {
        return try {
            val users = adminKeycloak.realm(realm).users().search(email, true)
            users.firstOrNull { it.email.equals(email, ignoreCase = true) }
        } catch (e: Exception) {
            logger.warn("Failed to search for user by email: $email", e)
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
            throw KeycloakException("Failed to update user in Keycloak: \${e.message}", e)
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
     * Assign a realm role to a user (using admin client)
     * This should be used for system initialization
     */
    fun assignRealmRoleAsAdmin(keycloakId: String, roleName: String) {
        assignRealmRoleInternal(adminKeycloak, keycloakId, roleName)
    }

    /**
     * Assign a realm role to a user (using service account)
     */
    fun assignRealmRole(keycloakId: String, roleName: String) {
        assignRealmRoleInternal(keycloak, keycloakId, roleName)
    }

    /**
     * Internal method to assign realm role with specified Keycloak client
     */
    private fun assignRealmRoleInternal(keycloakClient: Keycloak, keycloakId: String, roleName: String) {
        logger.info("Assigning realm role \'$roleName\' to Keycloak user: $keycloakId")
        try {
            val userResource = keycloakClient.realm(realm).users().get(keycloakId)
            val role = keycloakClient.realm(realm).roles().get(roleName).toRepresentation()
            userResource.roles().realmLevel().add(listOf(role))
            logger.info("Realm role \'$roleName\' assigned to user: $keycloakId")
        } catch (e: Exception) {
            logger.error("Failed to assign realm role \'$roleName\' to user: $keycloakId", e)
            throw KeycloakException("Failed to assign realm role: \${e.message}", e)
        }
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

    /**
     * Authenticate user with Keycloak and obtain access token
     * @return LoginResponse with access token and refresh token
     */
    fun authenticateUser(email: String, password: String): LoginResponse {
        val tokenUrl = "$serverUrl/realms/$realm/protocol/openid-connect/token"
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "password")
        body.add("client_id", serviceClientId)
        body.add("client_secret", serviceClientSecret)
        body.add("username", email)
        body.add("password", password)

        val request = HttpEntity(body, headers)
        val restTemplate = RestTemplate()
        
        try {
            val response = restTemplate.postForEntity(tokenUrl, request, Map::class.java)
            val responseBody = response.body ?: throw KeycloakException("Failed to authenticate: empty response")

            return LoginResponse(
                accessToken = responseBody["access_token"] as String,
                refreshToken = responseBody["refresh_token"] as String,
                expiresIn = responseBody["expires_in"] as Int,
                refreshExpiresIn = responseBody["refresh_expires_in"] as Int,
                tokenType = (responseBody["token_type"] as? String) ?: "Bearer"
            )
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            val errorMessage = extractErrorMessageFromResponse(e) ?: "Authentication failed"
            logger.error("Failed to authenticate user: $errorMessage", e)
            throw KeycloakException(errorMessage, e)
        } catch (e: Exception) {
            logger.error("Failed to authenticate user: \${e.message}", e)
            throw KeycloakException("Authentication failed: \${e.message}", e)
        }
    }

    fun refreshToken(refreshToken: String): LoginResponse {
        val tokenUrl = "$serverUrl/realms/$realm/protocol/openid-connect/token"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "refresh_token")
        body.add("client_id", serviceClientId)
        body.add("client_secret", serviceClientSecret)
        body.add("refresh_token", refreshToken)

        val request = HttpEntity(body, headers)
        val restTemplate = RestTemplate()

        try {
            val response = restTemplate.postForEntity(tokenUrl, request, Map::class.java)
            val responseBody = response.body ?: throw KeycloakException("Failed to refresh token: empty response")

            return LoginResponse(
                accessToken = responseBody["access_token"] as String,
                refreshToken = responseBody["refresh_token"] as String,
                expiresIn = responseBody["expires_in"] as Int,
                refreshExpiresIn = responseBody["refresh_expires_in"] as Int,
                tokenType = (responseBody["token_type"] as? String) ?: "Bearer"
            )
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            val errorMessage = extractErrorMessageFromResponse(e) ?: "Token refresh failed"
            logger.error("Failed to refresh token: $errorMessage", e)
            throw KeycloakException(errorMessage, e)
        } catch (e: Exception) {
            logger.error("Failed to refresh token: \${e.message}", e)
            throw KeycloakException("Token refresh failed: \${e.message}", e)
        }
    }

    /**
     * Extract error message from Keycloak error response
     * Keycloak returns errors in JSON format with 'error' and 'error_description' fields
     */
    private fun extractErrorMessageFromResponse(exception: org.springframework.web.client.HttpClientErrorException): String? {
        return try {
            val responseBody = exception.responseBodyAsString
            if (responseBody.isNotBlank()) {
                // Try to parse JSON response from Keycloak
                // Keycloak error format: {"error":"invalid_grant","error_description":"Invalid user credentials"}
                val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
                @Suppress("UNCHECKED_CAST")
                val errorMap = objectMapper.readValue(responseBody, Map::class.java) as? Map<String, Any>
                errorMap?.get("error_description") as? String
                    ?: errorMap?.get("error") as? String
            } else {
                null
            }
        } catch (e: Exception) {
            logger.debug("Could not parse error response body: \${e.message}")
            null
        }
    }
}

class KeycloakException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class UserAlreadyExistsException(message: String) : RuntimeException(message)
