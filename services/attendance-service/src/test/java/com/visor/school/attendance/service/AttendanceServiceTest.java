package com.visor.school.attendance.service;

import com.visor.school.attendance.event.AttendanceEventPublisher;
import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceSessionStatus;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.repository.AttendanceRepository;
import com.visor.school.attendance.repository.AttendanceSessionRepository;
import com.visor.school.attendance.service.AttendanceService.AttendanceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceSessionRepository attendanceSessionRepository;

    @Mock
    private AttendanceEventPublisher attendanceEventPublisher;

    private AttendanceService attendanceService;

    private final UUID testStudentId = UUID.randomUUID();
    private final UUID testClassId = UUID.randomUUID();
    private final UUID testTeacherId = UUID.randomUUID();
    private final LocalDate testDate = LocalDate.now();

    @BeforeEach
    void setup() {
        attendanceService = new AttendanceService(
            attendanceRepository,
            attendanceSessionRepository,
            attendanceEventPublisher,
            null
        );
    }

    @Test
    void shouldMarkAttendanceDirectly() {
        // Given
        when(attendanceRepository.findByStudentIdAndClassIdAndDate(testStudentId, testClassId, testDate))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(attendanceEventPublisher).publishAttendanceMarked(any());

        // When
        AttendanceRecord result = attendanceService.markAttendanceDirectly(
            testStudentId,
            testClassId,
            testDate,
            AttendanceStatus.PRESENT,
            testTeacherId,
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(testStudentId, result.getStudentId());
        assertEquals(AttendanceStatus.PRESENT, result.getStatus());
        assertEquals(testTeacherId, result.getMarkedBy());
        assertTrue(result.isDirectMarking());
        verify(attendanceRepository).save(any(AttendanceRecord.class));
        verify(attendanceEventPublisher).publishAttendanceMarked(any());
    }

    @Test
    void shouldUpdateExistingAttendanceRecord() {
        // Given
        AttendanceRecord existingRecord = new AttendanceRecord(
            testStudentId,
            testClassId,
            testDate,
            AttendanceStatus.PRESENT,
            testTeacherId
        );
        when(attendanceRepository.findByStudentIdAndClassIdAndDate(testStudentId, testClassId, testDate))
            .thenReturn(Optional.of(existingRecord));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AttendanceRecord result = attendanceService.markAttendanceDirectly(
            testStudentId,
            testClassId,
            testDate,
            AttendanceStatus.ABSENT,
            testTeacherId,
            null
        );

        // Then
        assertEquals(AttendanceStatus.ABSENT, result.getStatus());
        verify(attendanceRepository).save(existingRecord);
    }

    @Test
    void shouldCreateAttendanceSession() {
        // Given
        when(attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate))
            .thenReturn(Optional.empty());
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AttendanceSession result = attendanceService.createSession(
            testClassId,
            testDate,
            testTeacherId
        );

        // Then
        assertNotNull(result);
        assertEquals(testClassId, result.getClassId());
        assertEquals(testDate, result.getDate());
        assertEquals(AttendanceSessionStatus.PENDING, result.getStatus());
        verify(attendanceSessionRepository).save(any(AttendanceSession.class));
    }

    @Test
    void shouldThrowExceptionWhenSessionAlreadyExists() {
        // Given
        AttendanceSession existingSession = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        existingSession.delegateTo(UUID.randomUUID());

        when(attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate))
            .thenReturn(Optional.of(existingSession));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.createSession(testClassId, testDate, testTeacherId);
        });
    }

    @Test
    void shouldDelegateSessionToClassLeader() {
        // Given
        UUID sessionId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        // Force ID for mocking findById
        // In a real integration test, ID is generated on save. Here we rely on the object or mock behavior.
        // But AttendanceSession generates ID in constructor? 
        // Checking AttendanceSession.java: `this.id = UUID.randomUUID();` Correct.
        
        // We need to ensure the session returned by findById matches what we expect
        // Java objects are pass-by-reference, so modifying it inside service will modify this instance.
        
        when(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(attendanceEventPublisher).publishSessionDelegated(any(), any());

        // When
        AttendanceSession result = attendanceService.delegateToClassLeader(sessionId, classLeaderId, testClassId);

        // Then
        assertEquals(classLeaderId, result.getDelegatedTo());
        verify(attendanceSessionRepository).save(session);
        verify(attendanceEventPublisher).publishSessionDelegated(any(), eq(classLeaderId));
    }

    @Test
    void shouldCollectAttendanceByClassLeader() {
        // Given
        UUID sessionId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        session.delegateTo(classLeaderId);
        
        List<AttendanceEntry> attendanceEntries = Collections.singletonList(
            new AttendanceEntry(
                testStudentId,
                AttendanceStatus.PRESENT,
                null
            )
        );

        when(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.findByStudentIdAndClassIdAndDate(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(attendanceEventPublisher).publishSessionCollected(any());

        // When
        AttendanceSession result = attendanceService.collectAttendanceByClassLeader(
            sessionId,
            classLeaderId,
            attendanceEntries
        );

        // Then
        assertEquals(AttendanceSessionStatus.COLLECTED, result.getStatus());
        assertNotNull(result.getCollectedAt());
        verify(attendanceRepository).save(any(AttendanceRecord.class));
        verify(attendanceEventPublisher).publishSessionCollected(any());
    }

    @Test
    void shouldApproveSession() {
        // Given
        UUID sessionId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();

        when(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(attendanceEventPublisher).publishSessionApproved(any(), any());

        // When
        AttendanceSession result = attendanceService.approveSession(sessionId, testTeacherId);

        // Then
        assertEquals(AttendanceSessionStatus.APPROVED, result.getStatus());
        assertEquals(testTeacherId, result.getApprovedBy());
        verify(attendanceEventPublisher).publishSessionApproved(any(), eq(testTeacherId));
    }

    @Test
    void shouldRejectSessionWithReason() {
        // Given
        UUID sessionId = UUID.randomUUID();
        AttendanceSession session = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();

        when(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(attendanceEventPublisher).publishSessionRejected(any(), any(), any());

        // When
        AttendanceSession result = attendanceService.rejectSession(sessionId, testTeacherId, "Incomplete data");

        // Then
        assertEquals(AttendanceSessionStatus.REJECTED, result.getStatus());
        assertEquals(testTeacherId, result.getRejectedBy());
        assertEquals("Incomplete data", result.getRejectionReason());
        verify(attendanceEventPublisher).publishSessionRejected(any(), eq(testTeacherId), eq("Incomplete data"));
    }
}
