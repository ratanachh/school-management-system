package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentEntryTest {

    @Test
    void shouldCreateEnrollmentEntryWithRequiredFields() {
        EnrollmentEntry entry = new EnrollmentEntry(
                "2024-2025",
                Term.FIRST_TERM,
                9,
                LocalDate.of(2024, 9, 1),
                EnrollmentStatus.ENROLLED
        );

        assertEquals("2024-2025", entry.getAcademicYear());
        assertEquals(Term.FIRST_TERM, entry.getTerm());
        assertEquals(9, entry.getGradeLevel());
        assertEquals(LocalDate.of(2024, 9, 1), entry.getEnrollmentDate());
        assertEquals(EnrollmentStatus.ENROLLED, entry.getStatus());
    }

    @Test
    void shouldAcceptAllTermValues() {
        List<Term> terms = List.of(
                Term.FIRST_TERM,
                Term.SECOND_TERM,
                Term.THIRD_TERM,
                Term.FULL_YEAR
        );

        for (Term term : terms) {
            EnrollmentEntry entry = new EnrollmentEntry(
                    "2024-2025",
                    term,
                    9,
                    LocalDate.of(2024, 9, 1),
                    EnrollmentStatus.ENROLLED
            );

            assertEquals(term, entry.getTerm());
        }
    }

    @Test
    void shouldAcceptAllEnrollmentStatusValues() {
        List<EnrollmentStatus> statuses = List.of(
                EnrollmentStatus.ENROLLED,
                EnrollmentStatus.GRADUATED,
                EnrollmentStatus.TRANSFERRED,
                EnrollmentStatus.WITHDRAWN
        );

        for (EnrollmentStatus status : statuses) {
            EnrollmentEntry entry = new EnrollmentEntry(
                    "2024-2025",
                    Term.FIRST_TERM,
                    9,
                    LocalDate.of(2024, 9, 1),
                    status
            );

            assertEquals(status, entry.getStatus());
        }
    }

    @Test
    void shouldAcceptGradeLevels1To12() {
        for (int grade = 1; grade <= 12; grade++) {
            EnrollmentEntry entry = new EnrollmentEntry(
                    "2024-2025",
                    Term.FIRST_TERM,
                    grade,
                    LocalDate.of(2024, 9, 1),
                    EnrollmentStatus.ENROLLED
            );

            assertEquals(grade, entry.getGradeLevel());
        }
    }
}
