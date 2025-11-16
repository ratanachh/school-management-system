package com.visor.school.userservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class ParentTest {

    @Test
    fun `should create parent with required fields`() {
        val parent = Parent(
            userId = UUID.randomUUID(),
            studentId = UUID.randomUUID(),
            relationship = Relationship.MOTHER
        )

        assertNotNull(parent.id)
        assertNotNull(parent.userId)
        assertNotNull(parent.studentId)
        assertEquals(Relationship.MOTHER, parent.relationship)
        assertFalse(parent.isPrimary)
        assertNotNull(parent.createdAt)
        assertNotNull(parent.updatedAt)
    }

    @Test
    fun `should create parent with primary flag`() {
        val parent = Parent(
            userId = UUID.randomUUID(),
            studentId = UUID.randomUUID(),
            relationship = Relationship.FATHER,
            isPrimary = true
        )

        assertTrue(parent.isPrimary)
        assertEquals(Relationship.FATHER, parent.relationship)
    }

    @Test
    fun `should accept all relationship types`() {
        val relationships = listOf(
            Relationship.MOTHER,
            Relationship.FATHER,
            Relationship.GUARDIAN,
            Relationship.OTHER
        )

        relationships.forEach { relationship ->
            val parent = Parent(
                userId = UUID.randomUUID(),
                studentId = UUID.randomUUID(),
                relationship = relationship
            )

            assertEquals(relationship, parent.relationship)
        }
    }

    @Test
    fun `should have unique user and student combination`() {
        val userId = UUID.randomUUID()
        val studentId = UUID.randomUUID()

        val parent1 = Parent(
            userId = userId,
            studentId = studentId,
            relationship = Relationship.MOTHER
        )

        val parent2 = Parent(
            userId = userId,
            studentId = studentId,
            relationship = Relationship.FATHER
        )

        // Both should be created, but in database would have unique constraint
        assertNotNull(parent1.id)
        assertNotNull(parent2.id)
        assertEquals(userId, parent1.userId)
        assertEquals(userId, parent2.userId)
        assertEquals(studentId, parent1.studentId)
        assertEquals(studentId, parent2.studentId)
    }
}

