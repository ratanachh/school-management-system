package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcademicStandingTest {

    @Test
    void shouldHaveAllRequiredAcademicStandingValues() {
        AcademicStanding[] standings = AcademicStanding.values();

        assertTrue(contains(standings, AcademicStanding.GOOD_STANDING));
        assertTrue(contains(standings, AcademicStanding.PROBATION));
        assertTrue(contains(standings, AcademicStanding.SUSPENDED));
        assertTrue(contains(standings, AcademicStanding.GRADUATED));
        assertEquals(4, standings.length);
    }

    @Test
    void shouldHaveCorrectStandingNames() {
        assertEquals("GOOD_STANDING", AcademicStanding.GOOD_STANDING.name());
        assertEquals("PROBATION", AcademicStanding.PROBATION.name());
        assertEquals("SUSPENDED", AcademicStanding.SUSPENDED.name());
        assertEquals("GRADUATED", AcademicStanding.GRADUATED.name());
    }

    private boolean contains(AcademicStanding[] array, AcademicStanding value) {
        for (AcademicStanding standing : array) {
            if (standing == value) {
                return true;
            }
        }
        return false;
    }
}
