package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AcademicStandingTest {

    @Test
    fun `should have all required academic standing values`() {
        val standings = AcademicStanding.values()

        assertTrue(standings.contains(AcademicStanding.GOOD_STANDING))
        assertTrue(standings.contains(AcademicStanding.PROBATION))
        assertTrue(standings.contains(AcademicStanding.SUSPENDED))
        assertTrue(standings.contains(AcademicStanding.GRADUATED))
        assertEquals(4, standings.size)
    }

    @Test
    fun `should have correct standing names`() {
        assertEquals("GOOD_STANDING", AcademicStanding.GOOD_STANDING.name)
        assertEquals("PROBATION", AcademicStanding.PROBATION.name)
        assertEquals("SUSPENDED", AcademicStanding.SUSPENDED.name)
        assertEquals("GRADUATED", AcademicStanding.GRADUATED.name)
    }
}

