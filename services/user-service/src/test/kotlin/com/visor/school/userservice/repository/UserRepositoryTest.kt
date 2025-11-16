package com.visor.school.userservice.repository

import com.visor.school.userservice.model.AccountStatus
import com.visor.school.userservice.model.User
import com.visor.school.userservice.model.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository
) {

    @Test
    fun `should save and find user by id`() {
        val user = User(
            keycloakId = "keycloak-123",
            email = "test@example.com",
            role = UserRole.TEACHER,
            firstName = "John",
            lastName = "Doe"
        )

        val saved = userRepository.save(user)
        val found = userRepository.findById(saved.id)

        assertTrue(found.isPresent)
        assertEquals(saved.id, found.get().id)
        assertEquals("keycloak-123", found.get().keycloakId)
        assertEquals("test@example.com", found.get().email)
    }

    @Test
    fun `should find user by keycloakId`() {
        val user = User(
            keycloakId = "keycloak-456",
            email = "teacher@example.com",
            role = UserRole.TEACHER,
            firstName = "Jane",
            lastName = "Smith"
        )

        userRepository.save(user)
        val found = userRepository.findByKeycloakId("keycloak-456")

        assertTrue(found.isPresent)
        assertEquals("keycloak-456", found.get().keycloakId)
        assertEquals("teacher@example.com", found.get().email)
    }

    @Test
    fun `should find user by email`() {
        val user = User(
            keycloakId = "keycloak-789",
            email = "student@example.com",
            role = UserRole.STUDENT,
            firstName = "Bob",
            lastName = "Johnson"
        )

        userRepository.save(user)
        val found = userRepository.findByEmail("student@example.com")

        assertTrue(found.isPresent)
        assertEquals("student@example.com", found.get().email)
        assertEquals("keycloak-789", found.get().keycloakId)
    }

    @Test
    fun `should return empty when keycloakId not found`() {
        val found = userRepository.findByKeycloakId("non-existent-keycloak-id")
        assertFalse(found.isPresent)
    }

    @Test
    fun `should return empty when email not found`() {
        val found = userRepository.findByEmail("nonexistent@example.com")
        assertFalse(found.isPresent)
    }

    @Test
    fun `should check if email exists`() {
        val user = User(
            keycloakId = "keycloak-999",
            email = "exists@example.com",
            role = UserRole.ADMINISTRATOR,
            firstName = "Admin",
            lastName = "User"
        )

        userRepository.save(user)

        assertTrue(userRepository.existsByEmail("exists@example.com"))
        assertFalse(userRepository.existsByEmail("notexists@example.com"))
    }

    @Test
    fun `should check if keycloakId exists`() {
        val user = User(
            keycloakId = "keycloak-111",
            email = "admin@example.com",
            role = UserRole.ADMINISTRATOR,
            firstName = "Admin",
            lastName = "User"
        )

        userRepository.save(user)

        assertTrue(userRepository.existsByKeycloakId("keycloak-111"))
        assertFalse(userRepository.existsByKeycloakId("non-existent-keycloak-id"))
    }
}

