package com.visor.school.assessment.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AssessmentTypeTest {

    @Test
    fun `should have all required assessment types`() {
        val types = AssessmentType.values()

        assertTrue(types.contains(AssessmentType.TEST))
        assertTrue(types.contains(AssessmentType.QUIZ))
        assertTrue(types.contains(AssessmentType.ASSIGNMENT))
        assertTrue(types.contains(AssessmentType.PROJECT))
        assertTrue(types.contains(AssessmentType.EXAM))
        assertTrue(types.contains(AssessmentType.FINAL_EXAM))
        assertEquals(6, types.size)
    }

    @Test
    fun `should have correct type names`() {
        assertEquals("TEST", AssessmentType.TEST.name)
        assertEquals("QUIZ", AssessmentType.QUIZ.name)
        assertEquals("ASSIGNMENT", AssessmentType.ASSIGNMENT.name)
        assertEquals("PROJECT", AssessmentType.PROJECT.name)
        assertEquals("EXAM", AssessmentType.EXAM.name)
        assertEquals("FINAL_EXAM", AssessmentType.FINAL_EXAM.name)
    }
}

