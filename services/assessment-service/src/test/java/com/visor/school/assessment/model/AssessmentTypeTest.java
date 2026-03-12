package com.visor.school.assessment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentTypeTest {

    @Test
    void shouldHaveAllRequiredAssessmentTypes() {
        AssessmentType[] types = AssessmentType.values();

        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.TEST));
        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.QUIZ));
        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.ASSIGNMENT));
        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.PROJECT));
        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.EXAM));
        assertTrue(java.util.Arrays.asList(types).contains(AssessmentType.FINAL_EXAM));
        assertEquals(6, types.length);
    }

    @Test
    void shouldHaveCorrectTypeNames() {
        assertEquals("TEST", AssessmentType.TEST.name());
        assertEquals("QUIZ", AssessmentType.QUIZ.name());
        assertEquals("ASSIGNMENT", AssessmentType.ASSIGNMENT.name());
        assertEquals("PROJECT", AssessmentType.PROJECT.name());
        assertEquals("EXAM", AssessmentType.EXAM.name());
        assertEquals("FINAL_EXAM", AssessmentType.FINAL_EXAM.name());
    }
}
