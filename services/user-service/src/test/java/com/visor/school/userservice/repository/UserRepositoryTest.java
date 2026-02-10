package com.visor.school.userservice.repository;

import com.visor.school.userservice.model.AccountStatus;
import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(excludeAutoConfiguration = {
    RabbitAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-123",
            "test@example.com",
            roles,
            "John",
            "Doe"
        );

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("keycloak-123", found.get().getKeycloakId());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void shouldFindUserByKeycloakId() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.TEACHER);
        
        User user = new User(
            "keycloak-456",
            "teacher@example.com",
            roles,
            "Jane",
            "Smith"
        );

        userRepository.save(user);
        Optional<User> found = userRepository.findByKeycloakId("keycloak-456");

        assertTrue(found.isPresent());
        assertEquals("keycloak-456", found.get().getKeycloakId());
        assertEquals("teacher@example.com", found.get().getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.STUDENT);
        
        User user = new User(
            "keycloak-789",
            "student@example.com",
            roles,
            "Bob",
            "Johnson"
        );

        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("student@example.com");

        assertTrue(found.isPresent());
        assertEquals("student@example.com", found.get().getEmail());
        assertEquals("keycloak-789", found.get().getKeycloakId());
    }

    @Test
    void shouldReturnEmptyWhenKeycloakIdNotFound() {
        Optional<User> found = userRepository.findByKeycloakId("non-existent-keycloak-id");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckIfEmailExists() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ADMINISTRATOR);
        
        User user = new User(
            "keycloak-999",
            "exists@example.com",
            roles,
            "Admin",
            "User"
        );

        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }

    @Test
    void shouldCheckIfKeycloakIdExists() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ADMINISTRATOR);
        
        User user = new User(
            "keycloak-111",
            "admin@example.com",
            roles,
            "Admin",
            "User"
        );

        userRepository.save(user);

        assertTrue(userRepository.existsByKeycloakId("keycloak-111"));
        assertFalse(userRepository.existsByKeycloakId("non-existent-keycloak-id"));
    }
}
