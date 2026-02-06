package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ClassTest {
    @Test
    void shouldCreateHomeroomClassForGrades1To6() {
        Class homeroomClass = new Class("Grade 3 Homeroom", ClassType.HOMEROOM, null, 3, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertEquals(ClassType.HOMEROOM, homeroomClass.getClassType());
        assertEquals(3, homeroomClass.getGradeLevel());
        assertNotNull(homeroomClass.getHomeroomTeacherId());
        assertNull(homeroomClass.getSubject());
        assertNull(homeroomClass.getClassTeacherId());
    }
    @Test
    void shouldThrowExceptionForHomeroomClassWithGradeAbove6() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Homeroom", ClassType.HOMEROOM, null, 7, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldThrowExceptionForHomeroomClassWithSubject() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Homeroom", ClassType.HOMEROOM, "Mathematics", 3, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldThrowExceptionForHomeroomClassWithClassTeacherId() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Homeroom", ClassType.HOMEROOM, null, 3, UUID.randomUUID(), UUID.randomUUID(), "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldCreateSubjectClassForAllGrades() {
        Class subjectClass = new Class("Mathematics 101", ClassType.SUBJECT, "Mathematics", 10, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertEquals(ClassType.SUBJECT, subjectClass.getClassType());
        assertEquals("Mathematics", subjectClass.getSubject());
        assertEquals(10, subjectClass.getGradeLevel());
        assertNull(subjectClass.getHomeroomTeacherId());
    }
    @Test
    void shouldCreateSubjectClassForGrades1To6() {
        Class subjectClass = new Class("Art Grade 3", ClassType.SUBJECT, "Art", 3, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertEquals(ClassType.SUBJECT, subjectClass.getClassType());
        assertEquals(3, subjectClass.getGradeLevel());
    }
    @Test
    void shouldThrowExceptionForSubjectClassWithoutSubject() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Subject", ClassType.SUBJECT, null, 10, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldThrowExceptionForSubjectClassWithHomeroomTeacherId() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Subject", ClassType.SUBJECT, "Mathematics", 10, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldThrowExceptionForGradeLevelBelow1() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Grade", ClassType.SUBJECT, "Mathematics", 0, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldThrowExceptionForGradeLevelAbove12() {
        assertThrows(IllegalArgumentException.class, () -> new Class("Invalid Grade", ClassType.SUBJECT, "Mathematics", 13, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED));
    }
    @Test
    void shouldUpdateClassStatus() throws InterruptedException {
        Class classEntity = new Class("Test Class", ClassType.SUBJECT, "Mathematics", 5, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertEquals(ClassStatus.SCHEDULED, classEntity.getStatus());
        var initialUpdatedAt = classEntity.getUpdatedAt();
        Thread.sleep(10);
        classEntity.updateStatus(ClassStatus.IN_PROGRESS);
        assertEquals(ClassStatus.IN_PROGRESS, classEntity.getStatus());
        assertTrue(classEntity.getUpdatedAt().isAfter(initialUpdatedAt));
    }
    @Test
    void shouldIncrementAndDecrementEnrollment() {
        Class classEntity = new Class("Test Class", ClassType.SUBJECT, "Mathematics", 5, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertEquals(0, classEntity.getCurrentEnrollment());
        classEntity.incrementEnrollment();
        assertEquals(1, classEntity.getCurrentEnrollment());
        classEntity.incrementEnrollment();
        assertEquals(2, classEntity.getCurrentEnrollment());
        classEntity.decrementEnrollment();
        assertEquals(1, classEntity.getCurrentEnrollment());
    }
    @Test
    void shouldThrowExceptionWhenDecrementingEnrollmentBelowZero() {
        Class classEntity = new Class("Test Class", ClassType.SUBJECT, "Mathematics", 5, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        assertThrows(IllegalArgumentException.class, classEntity::decrementEnrollment);
    }
}
