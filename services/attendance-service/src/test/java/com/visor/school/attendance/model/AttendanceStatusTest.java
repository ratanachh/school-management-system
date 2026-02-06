package com.visor.school.attendance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class AttendanceStatusTest {

    @Test
    void shouldHaveAllRequiredStatusValues() {
        AttendanceStatus[] statuses = AttendanceStatus.values();

        assertTrue(Arrays.asList(statuses).contains(AttendanceStatus.PRESENT));
        assertTrue(Arrays.asList(statuses).contains(AttendanceStatus.ABSENT));
        assertTrue(Arrays.asList(statuses).contains(AttendanceStatus.LATE));
        assertTrue(Arrays.asList(statuses).contains(AttendanceStatus.EXCUSED));
        assertEquals(4, statuses.length);
    }

    @Test
    void shouldHaveCorrectStatusNames() {
        assertEquals("PRESENT", AttendanceStatus.PRESENT.name());
        assertEquals("ABSENT", AttendanceStatus.ABSENT.name());
        assertEquals("LATE", AttendanceStatus.LATE.name());
        assertEquals("EXCUSED", AttendanceStatus.EXCUSED.name());
    }
}
