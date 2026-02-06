package com.visor.school.assessment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void shouldCreateGradeWithRequiredFields() {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("85.5"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        assertNotNull(grade.getId());
        assertEquals(0, new BigDecimal("85.5").compareTo(grade.getScore()));
        assertEquals(0, new BigDecimal("100.0").compareTo(grade.getTotalPoints()));
        assertEquals(0, new BigDecimal("85.5").compareTo(grade.getPercentage()));
        assertNull(grade.getLetterGrade());
        assertNull(grade.getNotes());
    }

    @Test
    void shouldCalculatePercentageCorrectly() {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("75.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        assertEquals(0, new BigDecimal("75.0").compareTo(grade.getPercentage()));
    }

    @Test
    void shouldCalculatePercentageForPartialPoints() {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("42.5"),
            new BigDecimal("50.0"),
            UUID.randomUUID()
        );

        assertEquals(0, new BigDecimal("85.0").compareTo(grade.getPercentage()));
    }

    @Test
    void shouldValidateScoreIsNotNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Grade(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("-10.0"),
                new BigDecimal("100.0"),
                UUID.randomUUID()
            );
        });
    }

    @Test
    void shouldValidateScoreDoesNotExceedTotalPoints() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Grade(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("150.0"),
                new BigDecimal("100.0"),
                UUID.randomUUID()
            );
        });
    }

    @Test
    void shouldUpdateGrade() throws InterruptedException {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("80.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        var initialUpdatedAt = grade.getUpdatedAt();
        UUID updatedBy = UUID.randomUUID();
        Thread.sleep(10);

        grade.updateScore(new BigDecimal("90.0"), updatedBy);

        assertEquals(0, new BigDecimal("90.0").compareTo(grade.getScore()));
        assertEquals(0, new BigDecimal("90.0").compareTo(grade.getPercentage()));
        assertEquals(updatedBy, grade.getUpdatedBy());
        assertTrue(grade.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldAssignLetterGrade() {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("92.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        grade.assignLetterGrade("A");

        assertEquals("A", grade.getLetterGrade());
    }

    @Test
    void shouldAddNotes() {
        Grade grade = new Grade(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("75.0"),
            new BigDecimal("100.0"),
            UUID.randomUUID()
        );

        grade.addNotes("Good effort, needs improvement");

        assertEquals("Good effort, needs improvement", grade.getNotes());
    }
}
