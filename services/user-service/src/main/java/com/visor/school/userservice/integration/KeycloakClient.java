package com.visor.school.userservice.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.userservice.dto.LoginResponse;

import jakarta.ws.rs.core.Response;

/**
 * Keycloak Admin API client for user management.
 * Handles user creation, password reset, and user management operations.
 * Excluded in "test" profile; tests use a mock from TestConfig.
 */
@Component
@Profile("!test")
public class KeycloakClient {

    private static final int MIN_REFRESH_TOKEN_LENGTH = 10;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String serverUrl;
    private final String realm;
    private final String serviceClientId;
    private final String serviceClientSecret;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminClientId;

    private final RestTemplate restTemplate;
    private Keycloak keycloak;
    private Keycloak adminKeycloak;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KeycloakClient(
        @Value("${keycloak.server-url}") String serverUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.service-client-id}") String serviceClientId,
        @Value("${keycloak.service-client-secret}") String serviceClientSecret,
        @Value("${keycloak.admin-username}") String adminUsername,
        @Value("${keycloak.admin-password}") String adminPassword,
        @Value("${keycloak.admin-client-id}") String adminClientId
    ) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.serviceClientId = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminClientId = adminClientId;
        this.restTemplate = new RestTemplate();
    }

    private synchronized Keycloak getKeycloak() {
        if (keycloak == null) {
            keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(serviceClientId)
                .clientSecret(serviceClientSecret)
                .grantType("client_credentials")
                .build();
        }
        return keycloak;
    }

    private synchronized Keycloak getAdminKeycloak() {
        if (adminKeycloak == null) {
            adminKeycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .grantType("password")
                .build();
        }
        return adminKeycloak;
    }

    public String createUser(String email, String firstName, String lastName, String password, boolean emailVerified, Map<String, String> attributes) {
        return createUserInternal(getKeycloak(), email, firstName, lastName, password, emailVerified, attributes);
    }

    public String createUserAsAdmin(String email, String firstName, String lastName, String password, boolean emailVerified, Map<String, String> attributes) {
        return createUserInternal(getAdminKeycloak(), email, firstName, lastName, password, emailVerified, attributes);
    }

    private String createUserInternal(Keycloak keycloakClient, String email, String firstName, String lastName, String password, boolean emailVerified, Map<String, String> attributes) {
        logger.info("Creating user in Keycloak: {}", email);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(email);
        userRepresentation.setFirstName(firstName);
        userRepresentation.setLastName(lastName);
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(emailVerified);
        userRepresentation.setUsername(email);

        if (attributes != null && !attributes.isEmpty()) {
            Map<String, List<String>> keycloakAttributes = new HashMap<>();
            attributes.forEach((key, value) -> keycloakAttributes.put(key, Collections.singletonList(value)));
            userRepresentation.setAttributes(keycloakAttributes);
            logger.info("Setting user attributes: {}", attributes.keySet());
        }

        try (Response response = keycloakClient.realm(realm).users().create(userRepresentation)) {
            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation().toString());
                logger.info("User created in Keycloak with ID: {}", userId);

                setPasswordInternal(keycloakClient, userId, password, false);
                return userId;
            } else if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException("User with email " + email + " already exists in Keycloak");
            } else {
                throw new KeycloakException("Failed to create user in Keycloak: " + response.getStatusInfo().getReasonPhrase());
            }
        }
    }

    public void resetPassword(String keycloakId, String newPassword, boolean temporary) {
        logger.info("Resetting password for Keycloak user: {}", keycloakId);
        setPasswordInternal(getKeycloak(), keycloakId, newPassword, temporary);
    }

    private void setPasswordInternal(Keycloak keycloakClient, String keycloakId, String password, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(temporary);

        keycloakClient.realm(realm).users().get(keycloakId).resetPassword(credential);
        logger.info("Password set for Keycloak user: {} (temporary: {})", keycloakId, temporary);
    }

    public UserRepresentation getUser(String keycloakId) {
        try {
            return getKeycloak().realm(realm).users().get(keycloakId).toRepresentation();
        } catch (Exception e) {
            logger.warn("User not found in Keycloak: {}", keycloakId, e);
            return null;
        }
    }

    public UserRepresentation getUserByEmail(String email) {
        try {
            List<UserRepresentation> users = getAdminKeycloak().realm(realm).users().search(email, true);
            return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            logger.warn("Failed to search for user by email: {}", email, e);
            return null;
        }
    }

    public void updateUser(String keycloakId, UserRepresentation userRepresentation) {
        logger.info("Updating user in Keycloak: {}", keycloakId);
        try {
            getKeycloak().realm(realm).users().get(keycloakId).update(userRepresentation);
            logger.info("User updated in Keycloak: {}", keycloakId);
        } catch (Exception e) {
            logger.error("Failed to update user in Keycloak: {}", keycloakId, e);
            throw new KeycloakException("Failed to update user in Keycloak: " + e.getMessage(), e);
        }
    }

    public void updateEmailVerification(String keycloakId, boolean verified) {
        logger.info("Updating email verification for Keycloak user: {} to {}", keycloakId, verified);
        UserResource userResource = getKeycloak().realm(realm).users().get(keycloakId);
        UserRepresentation representation = userResource.toRepresentation();
        representation.setEmailVerified(verified);
        userResource.update(representation);
    }

    public Set<String> getUserRealmRoles(String keycloakId) {
        try {
            UserResource userResource = getKeycloak().realm(realm).users().get(keycloakId);
            List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll();
            return roles.stream().map(RoleRepresentation::getName).collect(Collectors.toSet());
        } catch (Exception e) {
            logger.warn("Failed to get realm roles for user: {}", keycloakId, e);
            return Collections.emptySet();
        }
    }

    public void assignRealmRolesAsAdmin(String keycloakId, Set<String> roleNames) {
        assignRealmRolesInternal(getAdminKeycloak(), keycloakId, roleNames);
    }
    
    // Helper simple method for single role
    public void assignRealmRoleAsAdmin(String keycloakId, String roleName) {
        assignRealmRolesInternal(getAdminKeycloak(), keycloakId, Collections.singleton(roleName));
    }

    public void assignRealmRoles(String keycloakId, Set<String> roleNames) {
        assignRealmRolesInternal(getKeycloak(), keycloakId, roleNames);
    }

    public void removeRealmRoles(String keycloakId, Set<String> roleNames) {
        removeRealmRolesInternal(getKeycloak(), keycloakId, roleNames);
    }

    public void syncRealmRoles(String keycloakId, Set<String> targetRoles) {
        Set<String> currentRoles = getUserRealmRoles(keycloakId);
        
        Set<String> rolesToAdd = new HashSet<>(targetRoles);
        rolesToAdd.removeAll(currentRoles);
        
        Set<String> rolesToRemove = new HashSet<>(currentRoles);
        rolesToRemove.removeAll(targetRoles);

        if (!rolesToAdd.isEmpty()) {
            assignRealmRoles(keycloakId, rolesToAdd);
            logger.info("Added roles to Keycloak user {}: {}", keycloakId, rolesToAdd);
        }
        if (!rolesToRemove.isEmpty()) {
            removeRealmRoles(keycloakId, rolesToRemove);
            logger.info("Removed roles from Keycloak user {}: {}", keycloakId, rolesToRemove);
        }
    }

    private void assignRealmRolesInternal(Keycloak keycloakClient, String keycloakId, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) return;

        logger.info("Assigning realm roles '{}' to Keycloak user: {}", roleNames, keycloakId);
        try {
            UserResource userResource = keycloakClient.realm(realm).users().get(keycloakId);
            List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> keycloakClient.realm(realm).roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());
            
            userResource.roles().realmLevel().add(roles);
            logger.info("Realm roles '{}' assigned to user: {}", roleNames, keycloakId);
        } catch (Exception e) {
            logger.error("Failed to assign realm roles '{}' to user: {}", roleNames, keycloakId, e);
            throw new KeycloakException("Failed to assign realm roles: " + e.getMessage(), e);
        }
    }

    private void removeRealmRolesInternal(Keycloak keycloakClient, String keycloakId, Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) return;

        logger.info("Removing realm roles '{}' from Keycloak user: {}", roleNames, keycloakId);
        try {
            UserResource userResource = keycloakClient.realm(realm).users().get(keycloakId);
            List<RoleRepresentation> roles = roleNames.stream()
                .map(roleName -> keycloakClient.realm(realm).roles().get(roleName).toRepresentation())
                .collect(Collectors.toList());
            
            userResource.roles().realmLevel().remove(roles);
            logger.info("Realm roles '{}' removed from user: {}", roleNames, keycloakId);
        } catch (Exception e) {
            logger.error("Failed to remove realm roles '{}' from user: {}", roleNames, keycloakId, e);
            throw new KeycloakException("Failed to remove realm roles: " + e.getMessage(), e);
        }
    }

    private String extractUserIdFromLocation(String location) {
        if (location == null) {
            throw new KeycloakException("Location header is null");
        }
        return location.substring(location.lastIndexOf("/") + 1);
    }

    public LoginResponse authenticateUser(String email, String password) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", serviceClientId);
        body.add("client_secret", serviceClientSecret);
        body.add("username", email);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            Map<?, ?> responseBody = restTemplate.postForObject(tokenUrl, request, Map.class);
            if (responseBody == null) {
                throw new KeycloakException("Failed to authenticate: empty response");
            }

            return mapToLoginResponse(responseBody);

        } catch (HttpServerErrorException e) {
            String errorMessage = extractErrorMessageFromResponse(e);
            if (errorMessage == null) errorMessage = "Authentication server error";
            logger.error("Keycloak server error while authenticating user: {} (HTTP {})", errorMessage, e.getStatusCode());
            throw new KeycloakException("Authentication server is temporarily unavailable. Please try again later.", e);
        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessageFromResponse(e);
            if (errorMessage == null) errorMessage = "Authentication failed";
            logger.error("Failed to authenticate user: {}", errorMessage);
            throw new KeycloakException(errorMessage, e);
        } catch (RestClientException e) {
            logger.error("Network error while authenticating user: {}", e.getMessage(), e);
            throw new KeycloakException("Failed to connect to authentication server", e);
        } catch (Exception e) {
            logger.error("Failed to authenticate user: {}", e.getClass().getSimpleName());
            throw new KeycloakException("Authentication failed: " + e.getMessage(), e);
        }
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be blank");
        }
        if (refreshToken.length() < MIN_REFRESH_TOKEN_LENGTH) {
            throw new IllegalArgumentException("Invalid refresh token format");
        }

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", serviceClientId);
        body.add("client_secret", serviceClientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            Map<?, ?> responseBody = restTemplate.postForObject(tokenUrl, request, Map.class);
            if (responseBody == null) {
                throw new KeycloakException("Failed to refresh token: empty response");
            }

            return mapToLoginResponse(responseBody, refreshToken); // Reuse refreshToken if not returned

        } catch (HttpServerErrorException e) {
            String errorMessage = extractErrorMessageFromResponse(e);
            if (errorMessage == null) errorMessage = "Authentication server error";
            logger.error("Keycloak server error while refreshing token: {} (HTTP {})", errorMessage, e.getStatusCode());
            throw new KeycloakException("Authentication server is temporarily unavailable. Please try again later.", e);
        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessageFromResponse(e);
            if (errorMessage == null) errorMessage = "Token refresh failed";
            logger.error("Failed to refresh token: {} (HTTP {})", errorMessage, e.getStatusCode());
            
            if (e.getStatusCode().value() == 400) throw new KeycloakException("Invalid refresh token", e);
            if (e.getStatusCode().value() == 401) throw new KeycloakException("Authentication failed", e);
            if (e.getStatusCode().value() == 403) throw new KeycloakException("Access denied", e);
            
            throw new KeycloakException(errorMessage, e);
        } catch (Exception e) {
            logger.error("Unexpected error while refreshing token: {}", e.getClass().getSimpleName(), e);
            throw new KeycloakException("Token refresh failed: " + e.getMessage(), e);
        }
    }

    private LoginResponse mapToLoginResponse(Map<?, ?> responseBody) {
        return mapToLoginResponse(responseBody, null);
    }

    private LoginResponse mapToLoginResponse(Map<?, ?> responseBody, String originalRefreshToken) {
        String accessToken = (String) responseBody.get("access_token");
        if (accessToken == null) throw new KeycloakException("Failed to authenticate: access_token not found in response");

        String refreshToken = (String) responseBody.get("refresh_token");
        if (refreshToken == null && originalRefreshToken != null) {
            refreshToken = originalRefreshToken;
        } else if (refreshToken == null) {
             throw new KeycloakException("Failed to authenticate: refresh_token not found in response");
        }

        int expiresIn;
        Object expiresInObj = responseBody.get("expires_in");
        if (expiresInObj instanceof Number) {
            expiresIn = ((Number) expiresInObj).intValue();
        } else if (expiresInObj instanceof String) {
            expiresIn = Integer.parseInt((String) expiresInObj);
        } else {
            throw new KeycloakException("Failed to authenticate: expires_in not found or invalid");
        }

        int refreshExpiresIn;
        Object refreshExpiresInObj = responseBody.get("refresh_expires_in");
        if (refreshExpiresInObj instanceof Number) {
            refreshExpiresIn = ((Number) refreshExpiresInObj).intValue();
        } else if (refreshExpiresInObj instanceof String) {
            refreshExpiresIn = Integer.parseInt((String) refreshExpiresInObj);
        } else {
            refreshExpiresIn = expiresIn;
        }
        
        String tokenType = (String) responseBody.get("token_type");
        if (tokenType == null) tokenType = "Bearer";

        return new LoginResponse(accessToken, refreshToken, expiresIn, refreshExpiresIn, tokenType);
    }

    private String extractErrorMessageFromResponse(HttpStatusCodeException exception) {
        try {
            String responseBody = exception.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isBlank()) {
                Map<?, ?> errorMap = objectMapper.readValue(responseBody, Map.class);
                String errorDescription = (String) errorMap.get("error_description");
                if (errorDescription != null) return errorDescription;
                String error = (String) errorMap.get("error");
                return error;
            }
        } catch (Exception e) {
            logger.debug("Could not parse error response body: {}", e.getMessage());
        }
        return null;
    }
}
