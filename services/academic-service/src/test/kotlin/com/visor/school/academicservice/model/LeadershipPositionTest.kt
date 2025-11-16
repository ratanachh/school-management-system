package com.visor.school.academicservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LeadershipPositionTest {

    @Test
    fun `should have all required leadership positions`() {
        val positions = LeadershipPosition.values()

        assertTrue(positions.contains(LeadershipPosition.FIRST_LEADER))
        assertTrue(positions.contains(LeadershipPosition.SECOND_LEADER))
        assertTrue(positions.contains(LeadershipPosition.THIRD_LEADER))
        assertTrue(positions.contains(LeadershipPosition.NONE))
        assertEquals(4, positions.size)
    }

    @Test
    fun `should have correct position names`() {
        assertEquals("FIRST_LEADER", LeadershipPosition.FIRST_LEADER.name)
        assertEquals("SECOND_LEADER", LeadershipPosition.SECOND_LEADER.name)
        assertEquals("THIRD_LEADER", LeadershipPosition.THIRD_LEADER.name)
    }

    @Test
    fun `should have hierarchical order`() {
        val positions = LeadershipPosition.values()
        
        // First leader should be first in enum (if ordered)
        assertEquals(LeadershipPosition.FIRST_LEADER, positions[0])
        assertEquals(LeadershipPosition.SECOND_LEADER, positions[1])
        assertEquals(LeadershipPosition.THIRD_LEADER, positions[2])
        assertEquals(LeadershipPosition.NONE, positions[3])
    }
}

