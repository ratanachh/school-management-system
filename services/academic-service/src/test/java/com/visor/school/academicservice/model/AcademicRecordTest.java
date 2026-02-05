package com.visor.school.academicservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AcademicRecordTest {

    @Test
    void shouldCreateAcademicRecordWithRequiredFields() {
        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.4"),
                new BigDecimal("3.4"),
                24,
                120,
                AcademicStanding.GOOD_STANDING
        );

        // ID is null until entity is persisted
        assertNull(record.getId());
        assertNotNull(record.getStudentId());
        assertEquals(new BigDecimal("3.4"), record.getCumulativeGPA());
        assertEquals(24, record.getCreditsEarned());
        assertEquals(AcademicStanding.GOOD_STANDING, record.getAcademicStanding());
        assertNull(record.getGraduationDate());
    }

    @Test
    void shouldCreateAcademicRecordWithEnrollmentHistory() {
        List<EnrollmentEntry> enrollmentHistory = new ArrayList<>();
        enrollmentHistory.add(new EnrollmentEntry(
                "2023-2024",
                Term.FIRST_TERM,
                9,
                LocalDate.of(2023, 9, 1),
                EnrollmentStatus.ENROLLED
        ));
        enrollmentHistory.add(new EnrollmentEntry(
                "2024-2025",
                Term.FIRST_TERM,
                10,
                LocalDate.of(2024, 9, 1),
                EnrollmentStatus.ENROLLED
        ));

        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.5"),
                new BigDecimal("3.5"),
                30,
                120,
                AcademicStanding.GOOD_STANDING
        );
        record.setEnrollmentHistory(enrollmentHistory);

        assertEquals(2, record.getEnrollmentHistory().size());
        assertEquals(9, record.getEnrollmentHistory().get(0).getGradeLevel());
        assertEquals(10, record.getEnrollmentHistory().get(1).getGradeLevel());
    }

    @Test
    void shouldCreateAcademicRecordWithCompletedCourses() {
        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.5"),
                new BigDecimal("3.5"),
                0,
                120,
                AcademicStanding.GOOD_STANDING
        );

        CourseCompletion course1 = new CourseCompletion(
                "Mathematics 101",
                "Mathematics",
                9,
                "A",
                3,
                LocalDate.of(2024, 6, 15)
        );
        CourseCompletion course2 = new CourseCompletion(
                "English 101",
                "English",
                9,
                "B",
                3,
                LocalDate.of(2024, 6, 15)
        );

        record.getCompletedCourses().add(course1);
        record.getCompletedCourses().add(course2);

        assertEquals(2, record.getCompletedCourses().size());
        assertEquals("Mathematics 101", record.getCompletedCourses().get(0).getCourseName());
        assertEquals("English 101", record.getCompletedCourses().get(1).getCourseName());
    }

    @Test
    void shouldUpdateAcademicStanding() {
        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.4"),
                new BigDecimal("3.4"),
                24,
                120,
                AcademicStanding.GOOD_STANDING
        );

        record.setAcademicStanding(AcademicStanding.PROBATION);

        assertEquals(AcademicStanding.PROBATION, record.getAcademicStanding());
    }

    @Test
    void shouldMarkAsGraduated() {
        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.4"),
                new BigDecimal("3.4"),
                120,
                120,
                AcademicStanding.GOOD_STANDING
        );

        LocalDate graduationDate = LocalDate.of(2025, 6, 15);
        record.setGraduationDate(graduationDate);

        assertEquals(graduationDate, record.getGraduationDate());
    }

    @Test
    void shouldAddCompletedCourse() {
        AcademicRecord record = new AcademicRecord(
                UUID.randomUUID(),
                new BigDecimal("3.4"),
                new BigDecimal("3.4"),
                0,
                120,
                AcademicStanding.GOOD_STANDING
        );

        CourseCompletion course = new CourseCompletion(
                "Science 101",
                "Science",
                9,
                "A",
                3,
                LocalDate.of(2024, 6, 15)
        );

        record.getCompletedCourses().add(course);

        assertEquals(1, record.getCompletedCourses().size());
        assertEquals("Science 101", record.getCompletedCourses().get(0).getCourseName());
    }

    @Test
    void shouldAcceptAllAcademicStandingValues() {
        List<AcademicStanding> standings = List.of(
                AcademicStanding.GOOD_STANDING,
                AcademicStanding.PROBATION,
                AcademicStanding.SUSPENDED,
                AcademicStanding.GRADUATED
        );

        for (AcademicStanding standing : standings) {
            AcademicRecord record = new AcademicRecord(
                    UUID.randomUUID(),
                    new BigDecimal("3.0"),
                    new BigDecimal("3.0"),
                    0,
                    120,
                    standing
            );

            assertEquals(standing, record.getAcademicStanding());
        }
    }
}
