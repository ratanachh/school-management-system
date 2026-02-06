package com.visor.school.attendance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

class AttendanceRecordTest {

    @Test
    void shouldCreateDirectMarkingAttendanceRecord() {
        AttendanceRecord record = new AttendanceRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            AttendanceStatus.PRESENT,
            UUID.randomUUID()
        );

        assertNull(record.getId());
        assertEquals(AttendanceStatus.PRESENT, record.getStatus());
        assertNotNull(record.getMarkedBy());
        assertNull(record.getCollectedBy());
        assertNull(record.getSessionId());
        assertTrue(record.isDirectMarking());
        assertFalse(record.isSessionBased());
    }

    @Test
    void shouldCreateSessionBasedAttendanceRecord() {
        UUID sessionId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();

        AttendanceRecord record = new AttendanceRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            AttendanceStatus.PRESENT,
            classLeaderId,
            sessionId
        );

        assertNotNull(record.getCollectedBy());
        assertNotNull(record.getSessionId());
        assertNull(record.getMarkedBy());
        assertFalse(record.isDirectMarking());
        assertTrue(record.isSessionBased());
    }

    @Test
    void shouldThrowExceptionForFutureDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttendanceRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now().plusDays(1),
                AttendanceStatus.PRESENT,
                UUID.randomUUID()
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenBothMarkedByAndCollectedByAreSet() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttendanceRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                AttendanceStatus.PRESENT,
                UUID.randomUUID(), // markedBy
                UUID.randomUUID(), // collectedBy
                UUID.randomUUID(), // sessionId
                null, 
                null
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenSessionIdProvidedWithoutCollectedBy() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttendanceRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                AttendanceStatus.PRESENT,
                null,
                null, // Missing collectedBy
                UUID.randomUUID(), // sessionId provided
                null,
                null
            );
        });
    }

    @Test
    void shouldThrowExceptionWhenApprovedBySetWithoutSessionId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttendanceRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                AttendanceStatus.PRESENT,
                UUID.randomUUID(), // markedBy
                null,
                null, // Missing sessionId
                UUID.randomUUID(), // approvedBy set
                null
            );
        });
    }

    @Test
    void shouldUpdateStatusAndNotes() throws InterruptedException {
        AttendanceRecord record = new AttendanceRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            AttendanceStatus.PRESENT,
            UUID.randomUUID()
        );

        java.time.Instant initialUpdatedAt = record.getUpdatedAt();
        Thread.sleep(10);

        record.updateStatus(AttendanceStatus.ABSENT, UUID.randomUUID(), "Student was absent");

        assertEquals(AttendanceStatus.ABSENT, record.getStatus());
        assertEquals("Student was absent", record.getNotes());
        assertTrue(record.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    void shouldAcceptAllAttendanceStatuses() {
        AttendanceStatus[] statuses = new AttendanceStatus[]{
            AttendanceStatus.PRESENT,
            AttendanceStatus.ABSENT,
            AttendanceStatus.LATE,
            AttendanceStatus.EXCUSED
        };

        for (AttendanceStatus status : statuses) {
            AttendanceRecord record = new AttendanceRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                status,
                UUID.randomUUID()
            );

            assertEquals(status, record.getStatus());
        }
    }
}
