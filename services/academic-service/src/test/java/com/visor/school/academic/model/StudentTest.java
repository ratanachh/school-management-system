package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void shouldCreateStudentWithValidGradeLevel() {
        Student student = new Student(
                "STU-2025-001",
                UUID.randomUUID(),
                "John",
                "Doe",
                LocalDate.of(2010, 1, 1),
                5,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        // ID is null until entity is persisted
        assertNull(student.getId());
        assertEquals("STU-2025-001", student.getStudentId());
        assertEquals(5, student.getGradeLevel());
        assertEquals(EnrollmentStatus.ENROLLED, student.getEnrollmentStatus());
        assertNotNull(student.getEnrolledAt());
    }

    @Test
    void shouldAcceptGradeLevel1() {
        Student student = new Student(
                "STU-2025-002",
                UUID.randomUUID(),
                "Jane",
                "Smith",
                LocalDate.of(2018, 1, 1),
                1,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        assertEquals(1, student.getGradeLevel());
    }

    @Test
    void shouldAcceptGradeLevel12() {
        Student student = new Student(
                "STU-2025-003",
                UUID.randomUUID(),
                "Bob",
                "Johnson",
                LocalDate.of(2006, 1, 1),
                12,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        assertEquals(12, student.getGradeLevel());
    }

    @Test
    void shouldThrowExceptionForGradeLevelBelow1() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Student(
                    "STU-2025-004",
                    UUID.randomUUID(),
                    "Invalid",
                    "Grade",
                    LocalDate.of(2020, 1, 1),
                    0,
                    EnrollmentStatus.ENROLLED,
                    null,
                    null
            );
        });
    }

    @Test
    void shouldThrowExceptionForGradeLevelAbove12() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Student(
                    "STU-2025-005",
                    UUID.randomUUID(),
                    "Invalid",
                    "Grade",
                    LocalDate.of(2000, 1, 1),
                    13,
                    EnrollmentStatus.ENROLLED,
                    null,
                    null
            );
        });
    }

    @Test
    void shouldPromoteStudentToNextGrade() throws InterruptedException {
        Student student = new Student(
                "STU-2025-006",
                UUID.randomUUID(),
                "Promote",
                "Test",
                LocalDate.of(2010, 1, 1),
                5,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        var initialUpdatedAt = student.getUpdatedAt();
        Thread.sleep(10);

        student.promoteToNextGrade();

        assertEquals(6, student.getGradeLevel());
        assertTrue(student.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldThrowExceptionWhenPromotingGrade12Student() {
        Student student = new Student(
                "STU-2025-007",
                UUID.randomUUID(),
                "Senior",
                "Student",
                LocalDate.of(2006, 1, 1),
                12,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, student::promoteToNextGrade);
    }

    @Test
    void shouldUpdateEnrollmentStatus() throws InterruptedException {
        Student student = new Student(
                "STU-2025-008",
                UUID.randomUUID(),
                "Status",
                "Test",
                LocalDate.of(2010, 1, 1),
                5,
                EnrollmentStatus.ENROLLED,
                null,
                null
        );

        assertEquals(EnrollmentStatus.ENROLLED, student.getEnrollmentStatus());
        var initialUpdatedAt = student.getUpdatedAt();

        Thread.sleep(10);
        student.updateEnrollmentStatus(EnrollmentStatus.GRADUATED);

        assertEquals(EnrollmentStatus.GRADUATED, student.getEnrollmentStatus());
        assertTrue(student.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldCreateStudentWithAddressAndEmergencyContact() {
        Address address = new Address(
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "USA"
        );

        EmergencyContact emergencyContact = new EmergencyContact(
                "Jane Doe",
                "Mother",
                "555-1234",
                "jane@example.com",
                null
        );

        Student student = new Student(
                "STU-2025-009",
                UUID.randomUUID(),
                "With",
                "Contact",
                LocalDate.of(2010, 1, 1),
                5,
                EnrollmentStatus.ENROLLED,
                address,
                emergencyContact
        );

        assertNotNull(student.getAddress());
        assertEquals("Springfield", student.getAddress().getCity());
        assertNotNull(student.getEmergencyContact());
        assertEquals("Jane Doe", student.getEmergencyContact().getName());
    }
}
