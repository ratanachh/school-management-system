package com.visor.school.attendance.repository;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private AttendanceRecord testRecord;
    private final UUID testStudentId = UUID.randomUUID();
    private final UUID testClassId = UUID.randomUUID();
    private final LocalDate testDate = LocalDate.now();

    @BeforeEach
    void setup() {
        testRecord = new AttendanceRecord(
            testStudentId,
            testClassId,
            testDate,
            AttendanceStatus.PRESENT,
            UUID.randomUUID()
        );
        entityManager.persistAndFlush(testRecord);
    }

    @Test
    void shouldFindAttendanceByStudentClassAndDate() {
        Optional<AttendanceRecord> found = attendanceRepository.findByStudentIdAndClassIdAndDate(
            testStudentId,
            testClassId,
            testDate
        );

        assertTrue(found.isPresent());
        assertEquals(testStudentId, found.get().getStudentId());
        assertEquals(testClassId, found.get().getClassId());
        assertEquals(testDate, found.get().getDate());
    }

    @Test
    void shouldFindAttendanceRecordsByClassAndDate() {
        AttendanceRecord record2 = new AttendanceRecord(
            UUID.randomUUID(),
            testClassId,
            testDate,
            AttendanceStatus.ABSENT,
            UUID.randomUUID()
        );
        entityManager.persistAndFlush(record2);

        List<AttendanceRecord> records = attendanceRepository.findByClassIdAndDate(testClassId, testDate);

        assertTrue(records.size() >= 2);
        assertTrue(records.stream().allMatch(it -> it.getClassId().equals(testClassId) && it.getDate().equals(testDate)));
    }

    @Test
    void shouldFindAttendanceRecordsByStudentAndClass() {
        AttendanceRecord record2 = new AttendanceRecord(
            testStudentId,
            testClassId,
            LocalDate.now().minusDays(1),
            AttendanceStatus.LATE,
            UUID.randomUUID()
        );
        entityManager.persistAndFlush(record2);

        List<AttendanceRecord> records = attendanceRepository.findByStudentIdAndClassId(testStudentId, testClassId);

        assertTrue(records.size() >= 2);
        assertTrue(records.stream().allMatch(it -> it.getStudentId().equals(testStudentId) && it.getClassId().equals(testClassId)));
    }

    @Test
    void shouldFindAttendanceRecordsBySessionId() {
        UUID sessionId = UUID.randomUUID();
        AttendanceRecord sessionRecord = new AttendanceRecord(
            UUID.randomUUID(),
            testClassId,
            testDate,
            AttendanceStatus.PRESENT,
            UUID.randomUUID(),
            sessionId
        );
        entityManager.persistAndFlush(sessionRecord);

        List<AttendanceRecord> records = attendanceRepository.findBySessionId(sessionId);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().allMatch(it -> it.getSessionId().equals(sessionId)));
    }

    @Test
    void shouldFindAttendanceRecordsByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        AttendanceRecord record2 = new AttendanceRecord(
            UUID.randomUUID(),
            testClassId,
            startDate,
            AttendanceStatus.PRESENT,
            UUID.randomUUID()
        );
        entityManager.persistAndFlush(record2);

        List<AttendanceRecord> records = attendanceRepository.findByClassIdAndDateRange(testClassId, startDate, endDate);

        assertFalse(records.isEmpty());
        assertTrue(records.stream().allMatch(it -> 
            it.getClassId().equals(testClassId) && 
            !it.getDate().isBefore(startDate) && 
            !it.getDate().isAfter(endDate)
        ));
    }

    @Test
    void shouldCountAttendanceByStatus() {
        AttendanceRecord absentRecord = new AttendanceRecord(
            UUID.randomUUID(),
            testClassId,
            testDate,
            AttendanceStatus.ABSENT,
            UUID.randomUUID()
        );
        entityManager.persistAndFlush(absentRecord);

        long presentCount = attendanceRepository.countByClassIdAndDateAndStatus(
            testClassId,
            testDate,
            AttendanceStatus.PRESENT
        );
        long absentCount = attendanceRepository.countByClassIdAndDateAndStatus(
            testClassId,
            testDate,
            AttendanceStatus.ABSENT
        );

        assertTrue(presentCount >= 1);
        assertTrue(absentCount >= 1);
    }
}
