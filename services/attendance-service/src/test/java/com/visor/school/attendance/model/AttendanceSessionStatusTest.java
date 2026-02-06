package com.visor.school.attendance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class AttendanceSessionStatusTest {

    @Test
    void shouldHaveAllRequiredStatusValues() {
        AttendanceSessionStatus[] statuses = AttendanceSessionStatus.values();

        assertTrue(Arrays.asList(statuses).contains(AttendanceSessionStatus.PENDING));
        assertTrue(Arrays.asList(statuses).contains(AttendanceSessionStatus.COLLECTED));
        assertTrue(Arrays.asList(statuses).contains(AttendanceSessionStatus.APPROVED));
        assertTrue(Arrays.asList(statuses).contains(AttendanceSessionStatus.REJECTED));
        assertEquals(4, statuses.length);
    }

    @Test
    void shouldHaveCorrectStatusNames() {
        assertEquals("PENDING", AttendanceSessionStatus.PENDING.name());
        assertEquals("COLLECTED", AttendanceSessionStatus.COLLECTED.name());
        assertEquals("APPROVED", AttendanceSessionStatus.APPROVED.name());
        assertEquals("REJECTED", AttendanceSessionStatus.REJECTED.name());
    }
}
