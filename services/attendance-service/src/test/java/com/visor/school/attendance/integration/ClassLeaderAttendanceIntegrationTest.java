package com.visor.school.attendance.integration;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceSessionStatus;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.repository.AttendanceRepository;
import com.visor.school.attendance.repository.AttendanceSessionRepository;
import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.attendance.service.AttendanceService.AttendanceEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for class leader attendance collection and approval flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassLeaderAttendanceIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldCompleteClassLeaderAttendanceCollectionAndApprovalFlow() {
        // Given
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        UUID studentId1 = UUID.randomUUID();
        UUID studentId2 = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // Step 1: Teacher creates session
        AttendanceSession session = attendanceService.createSession(
            classId,
            date,
            teacherId
        );

        assertEquals(AttendanceSessionStatus.PENDING, session.getStatus());
        assertNull(session.getDelegatedTo());

        // Step 2: Teacher delegates to class leader
        AttendanceSession delegatedSession = attendanceService.delegateToClassLeader(
            session.getId(),
            classLeaderId,
            classId
        );

        assertEquals(classLeaderId, delegatedSession.getDelegatedTo());
        assertEquals(AttendanceSessionStatus.PENDING, delegatedSession.getStatus());

        // Step 3: Class leader collects attendance
        List<AttendanceEntry> attendanceEntries = Arrays.asList(
            new AttendanceEntry(studentId1, AttendanceStatus.PRESENT, null),
            new AttendanceEntry(studentId2, AttendanceStatus.ABSENT, "Student absent")
        );

        AttendanceSession collectedSession = attendanceService.collectAttendanceByClassLeader(
            session.getId(),
            classLeaderId,
            attendanceEntries
        );

        assertEquals(AttendanceSessionStatus.COLLECTED, collectedSession.getStatus());
        assertNotNull(collectedSession.getCollectedAt());

        // Verify attendance records created
        List<AttendanceRecord> records = attendanceRepository.findBySessionId(session.getId());
        assertEquals(2, records.size());
        assertTrue(records.stream().anyMatch(it -> it.getStudentId().equals(studentId1) && it.getStatus() == AttendanceStatus.PRESENT));
        assertTrue(records.stream().anyMatch(it -> it.getStudentId().equals(studentId2) && it.getStatus() == AttendanceStatus.ABSENT));

        // Step 4: Teacher approves session
        AttendanceSession approvedSession = attendanceService.approveSession(session.getId(), teacherId);

        assertEquals(AttendanceSessionStatus.APPROVED, approvedSession.getStatus());
        assertEquals(teacherId, approvedSession.getApprovedBy());
        assertNotNull(approvedSession.getApprovedAt());

        // Verify records are linked to session
        List<AttendanceRecord> approvedRecords = attendanceRepository.findBySessionId(session.getId());
        assertTrue(approvedRecords.stream().allMatch(it -> it.getSessionId().equals(session.getId())));
    }

    @Test
    void shouldHandleSessionRejectionAndResubmission() {
        // Given
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // Step 1: Create and delegate session
        AttendanceSession session = attendanceService.createSession(classId, date, teacherId);
        attendanceService.delegateToClassLeader(session.getId(), classLeaderId, classId);

        // Step 2: Class leader collects
        AttendanceSession collectedSession = attendanceService.collectAttendanceByClassLeader(
            session.getId(),
            classLeaderId,
            List.of(new AttendanceEntry(studentId, AttendanceStatus.PRESENT, null))
        );

        assertEquals(AttendanceSessionStatus.COLLECTED, collectedSession.getStatus());

        // Step 3: Teacher rejects
        AttendanceSession rejectedSession = attendanceService.rejectSession(
            session.getId(),
            teacherId,
            "Incomplete attendance data"
        );

        assertEquals(AttendanceSessionStatus.REJECTED, rejectedSession.getStatus());
        assertEquals("Incomplete attendance data", rejectedSession.getRejectionReason());

        // Step 4: Class leader resubmits
        // We need to fetch from repository to get current state if we want to mimic entity reload, 
        // but 'rejectedSession' is detached/attached based on tx.
        // The service method saved it.
        
        AttendanceSession sessionEntity = attendanceSessionRepository.findById(session.getId()).orElseThrow();
        sessionEntity.resubmit();
        attendanceSessionRepository.save(sessionEntity);

        AttendanceSession resubmitted = attendanceSessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(AttendanceSessionStatus.COLLECTED, resubmitted.getStatus());
        assertNull(resubmitted.getRejectionReason());
    }

    @Test
    void shouldPreventCollectionByNonDelegatedLeader() {
        // Given
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        UUID otherStudentId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        AttendanceSession session = attendanceService.createSession(classId, date, teacherId);
        attendanceService.delegateToClassLeader(session.getId(), classLeaderId, classId);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            attendanceService.collectAttendanceByClassLeader(
                session.getId(),
                otherStudentId, // Not the delegated leader
                List.of(new AttendanceEntry(UUID.randomUUID(), AttendanceStatus.PRESENT, null))
            );
        });
    }
}
