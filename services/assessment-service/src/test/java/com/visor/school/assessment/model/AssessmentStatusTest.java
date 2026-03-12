package com.visor.school.assessment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentStatusTest {

    @Test
    void shouldHaveAllRequiredAssessmentStatusValues() {
        AssessmentStatus[] statuses = AssessmentStatus.values();

        assertTrue(java.util.Arrays.asList(statuses).contains(AssessmentStatus.DRAFT));
        assertTrue(java.util.Arrays.asList(statuses).contains(AssessmentStatus.PUBLISHED));
        assertTrue(java.util.Arrays.asList(statuses).contains(AssessmentStatus.GRADING));
        assertTrue(java.util.Arrays.asList(statuses).contains(AssessmentStatus.COMPLETED));
        assertEquals(4, statuses.length);
    }

    @Test
    void shouldHaveCorrectStatusNames() {
        assertEquals("DRAFT", AssessmentStatus.DRAFT.name());
        assertEquals("PUBLISHED", AssessmentStatus.PUBLISHED.name());
        assertEquals("GRADING", AssessmentStatus.GRADING.name());
        assertEquals("COMPLETED", AssessmentStatus.COMPLETED.name());
    }
}
