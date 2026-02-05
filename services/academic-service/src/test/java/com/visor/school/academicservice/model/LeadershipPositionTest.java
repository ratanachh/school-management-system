package com.visor.school.academicservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadershipPositionTest {

    @Test
    void shouldHaveAllRequiredLeadershipPositions() {
        LeadershipPosition[] positions = LeadershipPosition.values();

        assertTrue(contains(positions, LeadershipPosition.FIRST_LEADER));
        assertTrue(contains(positions, LeadershipPosition.SECOND_LEADER));
        assertTrue(contains(positions, LeadershipPosition.THIRD_LEADER));
        assertTrue(contains(positions, LeadershipPosition.NONE));
        assertEquals(4, positions.length);
    }

    @Test
    void shouldHaveCorrectPositionNames() {
        assertEquals("FIRST_LEADER", LeadershipPosition.FIRST_LEADER.name());
        assertEquals("SECOND_LEADER", LeadershipPosition.SECOND_LEADER.name());
        assertEquals("THIRD_LEADER", LeadershipPosition.THIRD_LEADER.name());
    }

    @Test
    void shouldHaveHierarchicalOrder() {
        LeadershipPosition[] positions = LeadershipPosition.values();

        // First leader should be first in enum (if ordered)
        assertEquals(LeadershipPosition.FIRST_LEADER, positions[0]);
        assertEquals(LeadershipPosition.SECOND_LEADER, positions[1]);
        assertEquals(LeadershipPosition.THIRD_LEADER, positions[2]);
        assertEquals(LeadershipPosition.NONE, positions[3]);
    }

    private boolean contains(LeadershipPosition[] array, LeadershipPosition value) {
        for (LeadershipPosition position : array) {
            if (position == value) {
                return true;
            }
        }
        return false;
    }
}
