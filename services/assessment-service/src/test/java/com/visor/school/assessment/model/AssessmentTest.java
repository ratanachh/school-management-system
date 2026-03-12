package com.visor.school.assessment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentTest {

    @Test
    void shouldCreateAssessmentWithRequiredFields() {
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Midterm Exam",
            AssessmentType.EXAM,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        assertNotNull(assessment.getId());
        assertEquals("Midterm Exam", assessment.getName());
        assertEquals(AssessmentType.EXAM, assessment.getType());
        assertEquals(0, new BigDecimal("100.0").compareTo(assessment.getTotalPoints()));
        assertEquals(AssessmentStatus.DRAFT, assessment.getStatus());
        assertNull(assessment.getDescription());
        assertNull(assessment.getWeight());
        assertNull(assessment.getDueDate());
    }

    @Test
    void shouldCreateAssessmentWithAllFields() {
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        LocalDate dueDate = LocalDate.now().plusDays(30);

        Assessment assessment = new Assessment(
            classId,
            "Final Project",
            AssessmentType.PROJECT,
            new BigDecimal("150.0"),
            teacherId,
            "Comprehensive project",
            new BigDecimal("30.0"),
            dueDate
        );

        assertEquals(classId, assessment.getClassId());
        assertEquals("Final Project", assessment.getName());
        assertEquals(AssessmentType.PROJECT, assessment.getType());
        assertEquals("Comprehensive project", assessment.getDescription());
        assertEquals(0, new BigDecimal("150.0").compareTo(assessment.getTotalPoints()));
        assertEquals(0, new BigDecimal("30.0").compareTo(assessment.getWeight()));
        assertEquals(dueDate, assessment.getDueDate());
        assertEquals(teacherId, assessment.getCreatedBy());
        assertEquals(AssessmentStatus.DRAFT, assessment.getStatus());
    }

    @Test
    void shouldValidateTotalPointsIsPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Assessment(
                UUID.randomUUID(),
                "Test",
                AssessmentType.TEST,
                new BigDecimal("-10.0"),
                UUID.randomUUID()
            );
        });
    }

    @Test
    void shouldValidateWeightIsBetween0And100() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Assessment(
                UUID.randomUUID(),
                "Test",
                AssessmentType.TEST,
                new BigDecimal("100.0"),
                UUID.randomUUID(),
                null,
                new BigDecimal("150.0"),
                null
            );
        });
    }

    @Test
    void shouldPublishAssessment() throws InterruptedException {
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Quiz 1",
            AssessmentType.QUIZ,
            new BigDecimal("50.0"),
            UUID.randomUUID()
        );

        var initialUpdatedAt = assessment.getUpdatedAt();
        Thread.sleep(10);

        assessment.publish();

        assertEquals(AssessmentStatus.PUBLISHED, assessment.getStatus());
        assertTrue(assessment.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldMarkAssessmentAsGrading() {
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        assessment.publish();

        assessment.markAsGrading();

        assertEquals(AssessmentStatus.GRADING, assessment.getStatus());
    }

    @Test
    void shouldCompleteAssessment() {
        Assessment assessment = new Assessment(
            UUID.randomUUID(),
            "Test",
            AssessmentType.TEST,
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );
        assessment.publish();
        assessment.markAsGrading();

        assessment.complete();

        assertEquals(AssessmentStatus.COMPLETED, assessment.getStatus());
    }

    @Test
    void shouldAcceptAllAssessmentTypes() {
        List<AssessmentType> types = Arrays.asList(
            AssessmentType.TEST,
            AssessmentType.QUIZ,
            AssessmentType.ASSIGNMENT,
            AssessmentType.PROJECT,
            AssessmentType.EXAM,
            AssessmentType.FINAL_EXAM
        );

        for (AssessmentType type : types) {
            Assessment assessment = new Assessment(
                UUID.randomUUID(),
                "Test " + type,
                type,
                new BigDecimal("100.0"),
                UUID.randomUUID()
            );

            assertEquals(type, assessment.getType());
        }
    }
}
