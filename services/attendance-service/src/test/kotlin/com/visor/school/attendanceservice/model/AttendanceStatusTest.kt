package com.visor.school.attendanceservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AttendanceStatusTest {

    @Test
    fun `should have all required status values`() {
        val statuses = AttendanceStatus.values()

        assertTrue(statuses.contains(AttendanceStatus.PRESENT))
        assertTrue(statuses.contains(AttendanceStatus.ABSENT))
        assertTrue(statuses.contains(AttendanceStatus.LATE))
        assertTrue(statuses.contains(AttendanceStatus.EXCUSED))
        assertEquals(4, statuses.size)
    }

    @Test
    fun `should have correct status names`() {
        assertEquals("PRESENT", AttendanceStatus.PRESENT.name)
        assertEquals("ABSENT", AttendanceStatus.ABSENT.name)
        assertEquals("LATE", AttendanceStatus.LATE.name)
        assertEquals("EXCUSED", AttendanceStatus.EXCUSED.name)
    }
}

