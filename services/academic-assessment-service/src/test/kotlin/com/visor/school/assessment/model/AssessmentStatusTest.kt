package com.visor.school.assessment.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AssessmentStatusTest {

    @Test
    fun `should have all required assessment status values`() {
        val statuses = AssessmentStatus.values()

        assertTrue(statuses.contains(AssessmentStatus.DRAFT))
        assertTrue(statuses.contains(AssessmentStatus.PUBLISHED))
        assertTrue(statuses.contains(AssessmentStatus.GRADING))
        assertTrue(statuses.contains(AssessmentStatus.COMPLETED))
        assertEquals(4, statuses.size)
    }

    @Test
    fun `should have correct status names`() {
        assertEquals("DRAFT", AssessmentStatus.DRAFT.name)
        assertEquals("PUBLISHED", AssessmentStatus.PUBLISHED.name)
        assertEquals("GRADING", AssessmentStatus.GRADING.name)
        assertEquals("COMPLETED", AssessmentStatus.COMPLETED.name)
    }
}

