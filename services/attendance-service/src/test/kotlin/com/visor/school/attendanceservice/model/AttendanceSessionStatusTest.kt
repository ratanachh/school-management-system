package com.visor.school.attendanceservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AttendanceSessionStatusTest {

    @Test
    fun `should have all required status values`() {
        val statuses = AttendanceSessionStatus.values()

        assertTrue(statuses.contains(AttendanceSessionStatus.PENDING))
        assertTrue(statuses.contains(AttendanceSessionStatus.COLLECTED))
        assertTrue(statuses.contains(AttendanceSessionStatus.APPROVED))
        assertTrue(statuses.contains(AttendanceSessionStatus.REJECTED))
        assertEquals(4, statuses.size)
    }

    @Test
    fun `should have correct status names`() {
        assertEquals("PENDING", AttendanceSessionStatus.PENDING.name)
        assertEquals("COLLECTED", AttendanceSessionStatus.COLLECTED.name)
        assertEquals("APPROVED", AttendanceSessionStatus.APPROVED.name)
        assertEquals("REJECTED", AttendanceSessionStatus.REJECTED.name)
    }
}

