package com.visor.school.attendance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

class AttendanceSessionTest {

    @Test
    void shouldCreateAttendanceSessionWithRequiredFields() {
        UUID classId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        AttendanceSession session = new AttendanceSession(classId, date, createdBy);
        // DelegatedTo is not in the constructor, set via method or left null
        
        assertNull(session.getId());
        assertEquals(AttendanceSessionStatus.PENDING, session.getStatus());
        assertNull(session.getDelegatedTo());
        assertNotNull(session.getCreatedBy());
        assertNull(session.getApprovedBy());
        assertNull(session.getRejectedBy());
    }

    @Test
    void shouldThrowExceptionForFutureDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttendanceSession(
                UUID.randomUUID(),
                LocalDate.now().plusDays(1),
                UUID.randomUUID()
            );
        });
    }

    @Test
    void shouldMarkSessionAsCollected() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        // Delegate first as required by markAsCollected
        session.delegateTo(UUID.randomUUID());

        session.markAsCollected();

        assertEquals(AttendanceSessionStatus.COLLECTED, session.getStatus());
        assertNotNull(session.getCollectedAt());
    }

    @Test
    void shouldThrowExceptionWhenMarkingCollectedWithoutDelegation() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        // Not delegated

        assertThrows(IllegalStateException.class, session::markAsCollected);
    }

    @Test
    void shouldApproveSession() {
        UUID teacherId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();

        session.approve(teacherId);

        assertEquals(AttendanceSessionStatus.APPROVED, session.getStatus());
        assertEquals(teacherId, session.getApprovedBy());
        assertNotNull(session.getApprovedAt());
        assertNull(session.getRejectedBy());
        assertNull(session.getRejectionReason());
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonCollectedSession() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        
        assertThrows(IllegalStateException.class, () -> session.approve(UUID.randomUUID()));
    }

    @Test
    void shouldRejectSessionWithReason() {
        UUID teacherId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();

        session.reject(teacherId, "Incomplete attendance data");

        assertEquals(AttendanceSessionStatus.REJECTED, session.getStatus());
        assertEquals(teacherId, session.getRejectedBy());
        assertEquals("Incomplete attendance data", session.getRejectionReason());
        assertNotNull(session.getRejectedAt());
        assertNull(session.getCollectedAt()); // Reset after rejection
    }

    @Test
    void shouldThrowExceptionWhenRejectingWithoutReason() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();

        assertThrows(IllegalArgumentException.class, () -> session.reject(UUID.randomUUID(), ""));
    }

    @Test
    void shouldResubmitRejectedSession() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();
        session.reject(UUID.randomUUID(), "Incomplete");

        session.resubmit();

        assertEquals(AttendanceSessionStatus.COLLECTED, session.getStatus());
        assertNotNull(session.getCollectedAt());
        assertNull(session.getRejectionReason());
        assertNull(session.getRejectedBy());
    }

    @Test
    void shouldThrowExceptionWhenResubmittingNonRejectedSession() {
        AttendanceSession session = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            UUID.randomUUID()
        );
        
        assertThrows(IllegalStateException.class, session::resubmit);
    }
}
