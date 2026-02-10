package com.visor.school.userservice.integration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import com.visor.school.userservice.repository.UserRepository;
import com.visor.school.userservice.service.UserService;

/**
 * Integration test for user registration flow
 * Tests: Keycloak user creation → User entity creation
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class UserRegistrationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCompleteUserRegistrationFlowWithKeycloakIntegration() {
        // Given
        String email = "integration-test-" + System.currentTimeMillis() + "@example.com";
        String firstName = "Integration";
        String lastName = "Test";
        String password = "TestPassword123!";

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);

        // When
        User user = userService.createUser(
            email,
            firstName,
            lastName,
            roles,
            password,
            null
        );

        // Then
        assertNotNull(user);
        assertNotNull(user.getKeycloakId());
        assertFalse(user.getKeycloakId().isBlank());
        assertEquals(email, user.getEmail());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertTrue(user.getRoles().contains(UserRole.STUDENT));

        // Verify user is persisted
        Optional<User> savedUser = userRepository.findById(user.getId());
        assertTrue(savedUser.isPresent());
        assertEquals(email, savedUser.get().getEmail());
    }

    @Test
    void shouldFailRegistrationWhenEmailAlreadyExists() {
        // Given
        String email = "duplicate-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        userService.createUser(
            email,
            "First",
            "User",
            roles,
            "Password123!",
            null
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(
                email,
                "Second",
                "User",
                roles,
                "Password123!",
                null
            );
        });
    }
}
