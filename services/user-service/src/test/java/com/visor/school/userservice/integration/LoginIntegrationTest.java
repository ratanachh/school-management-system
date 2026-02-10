package com.visor.school.userservice.integration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Integration test for login flow through Keycloak token endpoint
 * 
 * Note: This test requires Keycloak to be running or Testcontainers
 * TODO: Add Testcontainers support for Keycloak
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Requires Keycloak instance or Testcontainers setup")
class LoginIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldUpdateLastLoginWhenUserLogsIn() {
        // Given - Create user
        String email = "login-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = userService.createUser(
            email,
            "Login",
            "Test",
            roles,
            "Password123!",
            null
        );

        assertNull(user.getLastLoginAt());

        // When - Update last login (simulating login)
        userService.updateLastLogin(user.getId());

        // Then
        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertNotNull(updatedUser.get().getLastLoginAt());
    }

    @Test
    void shouldFindUserByEmailForLogin() {
        // Given - Create user
        String email = "login-email-test-" + System.currentTimeMillis() + "@example.com";
        
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = userService.createUser(
            email,
            "Email",
            "Test",
            roles,
            "Password123!",
            null
        );

        // When
        User foundUser = userService.findByEmail(email);

        // Then
        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
    }
}
