package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class ClassTest {

    @Test
    fun `should create homeroom class for grades 1-6`() {
        val homeroomClass = Class(
            className = "Grade 3 Homeroom",
            classType = ClassType.HOMEROOM,
            subject = null,
            gradeLevel = 3,
            homeroomTeacherId = UUID.randomUUID(),
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        assertEquals(ClassType.HOMEROOM, homeroomClass.classType)
        assertEquals(3, homeroomClass.gradeLevel)
        assertNotNull(homeroomClass.homeroomTeacherId)
        assertNull(homeroomClass.subject)
        assertNull(homeroomClass.classTeacherId)
    }

    @Test
    fun `should throw exception for homeroom class with grade above 6`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Homeroom",
                classType = ClassType.HOMEROOM,
                subject = null,
                gradeLevel = 7,
                homeroomTeacherId = UUID.randomUUID(),
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should throw exception for homeroom class with subject`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Homeroom",
                classType = ClassType.HOMEROOM,
                subject = "Mathematics", // Should be null for homeroom
                gradeLevel = 3,
                homeroomTeacherId = UUID.randomUUID(),
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should throw exception for homeroom class with classTeacherId`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Homeroom",
                classType = ClassType.HOMEROOM,
                subject = null,
                gradeLevel = 3,
                homeroomTeacherId = UUID.randomUUID(),
                classTeacherId = UUID.randomUUID(), // Should be null for homeroom
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should create subject class for all grades`() {
        val subjectClass = Class(
            className = "Mathematics 101",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 10,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        assertEquals(ClassType.SUBJECT, subjectClass.classType)
        assertEquals("Mathematics", subjectClass.subject)
        assertEquals(10, subjectClass.gradeLevel)
        assertNull(subjectClass.homeroomTeacherId)
    }

    @Test
    fun `should create subject class for grades 1-6`() {
        val subjectClass = Class(
            className = "Art Grade 3",
            classType = ClassType.SUBJECT,
            subject = "Art",
            gradeLevel = 3,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        assertEquals(ClassType.SUBJECT, subjectClass.classType)
        assertEquals(3, subjectClass.gradeLevel)
    }

    @Test
    fun `should throw exception for subject class without subject`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Subject",
                classType = ClassType.SUBJECT,
                subject = null, // Required for subject classes
                gradeLevel = 10,
                homeroomTeacherId = null,
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should throw exception for subject class with homeroomTeacherId`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Subject",
                classType = ClassType.SUBJECT,
                subject = "Mathematics",
                gradeLevel = 10,
                homeroomTeacherId = UUID.randomUUID(), // Should be null for subject classes
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should throw exception for grade level below 1`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Grade",
                classType = ClassType.SUBJECT,
                subject = "Mathematics",
                gradeLevel = 0, // Invalid
                homeroomTeacherId = null,
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should throw exception for grade level above 12`() {
        assertThrows<IllegalArgumentException> {
            Class(
                className = "Invalid Grade",
                classType = ClassType.SUBJECT,
                subject = "Mathematics",
                gradeLevel = 13, // Invalid
                homeroomTeacherId = null,
                classTeacherId = null,
                academicYear = "2024-2025",
                term = Term.FIRST_TERM,
                startDate = LocalDate.of(2024, 9, 1)
            )
        }
    }

    @Test
    fun `should update class status`() {
        val classEntity = Class(
            className = "Test Class",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 5,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            status = ClassStatus.SCHEDULED
        )

        assertEquals(ClassStatus.SCHEDULED, classEntity.status)
        val initialUpdatedAt = classEntity.updatedAt

        Thread.sleep(10)
        classEntity.updateStatus(ClassStatus.IN_PROGRESS)

        assertEquals(ClassStatus.IN_PROGRESS, classEntity.status)
        assertTrue(classEntity.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should increment and decrement enrollment`() {
        val classEntity = Class(
            className = "Test Class",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 5,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1),
            maxCapacity = 30
        )

        assertEquals(0, classEntity.currentEnrollment)

        classEntity.incrementEnrollment()
        assertEquals(1, classEntity.currentEnrollment)

        classEntity.incrementEnrollment()
        assertEquals(2, classEntity.currentEnrollment)

        classEntity.decrementEnrollment()
        assertEquals(1, classEntity.currentEnrollment)
    }

    @Test
    fun `should throw exception when decrementing enrollment below zero`() {
        val classEntity = Class(
            className = "Test Class",
            classType = ClassType.SUBJECT,
            subject = "Mathematics",
            gradeLevel = 5,
            homeroomTeacherId = null,
            classTeacherId = null,
            academicYear = "2024-2025",
            term = Term.FIRST_TERM,
            startDate = LocalDate.of(2024, 9, 1)
        )

        assertThrows<IllegalArgumentException> {
            classEntity.decrementEnrollment()
        }
    }
}

