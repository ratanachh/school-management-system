package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class TeacherTest {

    @Test
    fun `should create teacher with required fields`() {
        val teacher = Teacher(
            employeeId = "EMP-2025-001",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Mathematics", "Physics"),
            hireDate = LocalDate.of(2020, 1, 1)
        )

        // ID is null until entity is persisted
        assertNull(teacher.id)
        assertEquals("EMP-2025-001", teacher.employeeId)
        assertEquals(EmploymentStatus.ACTIVE, teacher.employmentStatus)
        assertEquals(2, teacher.subjectSpecializations.size)
        assertTrue(teacher.subjectSpecializations.contains("Mathematics"))
    }

    @Test
    fun `should require at least one subject specialization`() {
        assertThrows<IllegalArgumentException> {
            Teacher(
                employeeId = "EMP-2025-002",
                userId = UUID.randomUUID(),
                subjectSpecializations = emptyList(),
                hireDate = LocalDate.of(2020, 1, 1)
            )
        }
    }

    @Test
    fun `should accept multiple subject specializations`() {
        val teacher = Teacher(
            employeeId = "EMP-2025-003",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Mathematics", "Physics", "Chemistry"),
            hireDate = LocalDate.of(2020, 1, 1)
        )

        assertEquals(3, teacher.subjectSpecializations.size)
    }

    @Test
    fun `should accept qualifications`() {
        val teacher = Teacher(
            employeeId = "EMP-2025-004",
            userId = UUID.randomUUID(),
            qualifications = listOf("Bachelor's Degree", "Teaching Certificate"),
            subjectSpecializations = listOf("English"),
            hireDate = LocalDate.of(2020, 1, 1)
        )

        assertEquals(2, teacher.qualifications.size)
        assertTrue(teacher.qualifications.contains("Bachelor's Degree"))
    }

    @Test
    fun `should update employment status`() {
        val teacher = Teacher(
            employeeId = "EMP-2025-005",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("History"),
            hireDate = LocalDate.of(2020, 1, 1),
            employmentStatus = EmploymentStatus.ACTIVE
        )

        assertEquals(EmploymentStatus.ACTIVE, teacher.employmentStatus)
        val initialUpdatedAt = teacher.updatedAt

        Thread.sleep(10)
        teacher.updateEmploymentStatus(EmploymentStatus.ON_LEAVE)

        assertEquals(EmploymentStatus.ON_LEAVE, teacher.employmentStatus)
        assertTrue(teacher.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should accept all employment statuses`() {
        val statuses = listOf(
            EmploymentStatus.ACTIVE,
            EmploymentStatus.ON_LEAVE,
            EmploymentStatus.TERMINATED,
            EmploymentStatus.RETIRED
        )

        statuses.forEach { status ->
            val teacher = Teacher(
                employeeId = "EMP-${status.name}",
                userId = UUID.randomUUID(),
                subjectSpecializations = listOf("Test"),
                hireDate = LocalDate.of(2020, 1, 1),
                employmentStatus = status
            )

            assertEquals(status, teacher.employmentStatus)
        }
    }

    @Test
    fun `should accept department assignment`() {
        val teacher = Teacher(
            employeeId = "EMP-2025-006",
            userId = UUID.randomUUID(),
            subjectSpecializations = listOf("Science"),
            hireDate = LocalDate.of(2020, 1, 1),
            department = "Science Department"
        )

        assertEquals("Science Department", teacher.department)
    }
}

