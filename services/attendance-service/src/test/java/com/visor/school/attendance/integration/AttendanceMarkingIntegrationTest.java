package com.visor.school.attendance.integration;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.repository.AttendanceRepository;
import com.visor.school.attendance.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for direct attendance marking flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceMarkingIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldCompleteDirectAttendanceMarkingFlow() {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // When
        AttendanceRecord record = attendanceService.markAttendanceDirectly(
            studentId,
            classId,
            date,
            AttendanceStatus.PRESENT,
            teacherId,
            "Student present"
        );

        // Then
        assertNotNull(record);
        assertNotNull(record.getId());
        assertEquals(studentId, record.getStudentId());
        assertEquals(classId, record.getClassId());
        assertEquals(date, record.getDate());
        assertEquals(AttendanceStatus.PRESENT, record.getStatus());
        assertEquals(teacherId, record.getMarkedBy());
        assertTrue(record.isDirectMarking());
        assertFalse(record.isSessionBased());

        // Verify persisted
        Optional<AttendanceRecord> saved = attendanceRepository.findById(record.getId());
        assertTrue(saved.isPresent());
        assertEquals(AttendanceStatus.PRESENT, saved.get().getStatus());
    }

    @Test
    void shouldUpdateExistingAttendanceRecord() {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        attendanceService.markAttendanceDirectly(
            studentId,
            classId,
            date,
            AttendanceStatus.PRESENT,
            teacherId,
            null
        );

        // When
        AttendanceRecord updated = attendanceService.markAttendanceDirectly(
            studentId,
            classId,
            date,
            AttendanceStatus.ABSENT,
            teacherId,
            "Updated to absent"
        );

        // Then
        assertEquals(AttendanceStatus.ABSENT, updated.getStatus());
        assertEquals("Updated to absent", updated.getNotes());
    }

    @Test
    void shouldMarkMultipleStudentsAttendance() {
        // Given
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        // When
        AttendanceRecord record1 = attendanceService.markAttendanceDirectly(
            UUID.randomUUID(),
            classId,
            date,
            AttendanceStatus.PRESENT,
            teacherId,
            null
        );

        AttendanceRecord record2 = attendanceService.markAttendanceDirectly(
            UUID.randomUUID(),
            classId,
            date,
            AttendanceStatus.LATE,
            teacherId,
            null
        );

        // Then
        assertNotNull(record1);
        assertNotNull(record2);
        assertEquals(classId, record1.getClassId());
        assertEquals(classId, record2.getClassId());
        assertEquals(date, record1.getDate());
        assertEquals(date, record2.getDate());
    }

    @Test
    void shouldHandleDifferentAttendanceStatuses() {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        AttendanceStatus[] statuses = new AttendanceStatus[]{
            AttendanceStatus.PRESENT,
            AttendanceStatus.ABSENT,
            AttendanceStatus.LATE,
            AttendanceStatus.EXCUSED
        };

        // When & Then
        for (AttendanceStatus status : statuses) {
            AttendanceRecord record = attendanceService.markAttendanceDirectly(
                UUID.randomUUID(),
                classId,
                date,
                status,
                teacherId,
                null
            );

            assertEquals(status, record.getStatus());
        }
    }
}
