package com.visor.school.attendance.repository;

import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceSessionStatus;
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
class AttendanceSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    private AttendanceSession testSession;
    private final UUID testClassId = UUID.randomUUID();
    private final LocalDate testDate = LocalDate.now();
    private final UUID testTeacherId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        testSession = new AttendanceSession(
            testClassId,
            testDate,
            testTeacherId
        );
        // Using setter to set other fields if needed, but constructor sets PENDING status by default 
        // and others as null. The kotlin test had delegateTo in constructor but Java one doesn't seem to have it in the main constructor unless added.
        // Let's check AttendanceSession.java... 
        // It has AttendanceSession(classId, date, createdBy).
        
        // Wait, the Kotlin test had:
        // AttendanceSession(classId, date, delegatedTo, createdBy, status)
        
        // My Java implementation:
        // public AttendanceSession(UUID classId, LocalDate date, UUID createdBy) { ... }
        
        // I should set delegatedTo and status using setters if I want to match the test setup perfectly,
        // or just rely on defaults if they are enough.
        // In Kotlin test: delegatedTo = UUID.randomUUID(), status = PENDING.
        
        testSession.delegateTo(UUID.randomUUID());
        // Status is PENDING by default.
        
        entityManager.persistAndFlush(testSession);
    }

    @Test
    void shouldFindSessionByClassAndDate() {
        Optional<AttendanceSession> found = attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate);

        assertTrue(found.isPresent());
        assertEquals(testClassId, found.get().getClassId());
        assertEquals(testDate, found.get().getDate());
    }

    @Test
    void shouldFindSessionsByClassId() {
        AttendanceSession session2 = new AttendanceSession(
            testClassId,
            LocalDate.now().minusDays(1),
            testTeacherId
        );
        session2.delegateTo(UUID.randomUUID());
        entityManager.persistAndFlush(session2);

        List<AttendanceSession> sessions = attendanceSessionRepository.findByClassId(testClassId);

        assertTrue(sessions.size() >= 2);
        assertTrue(sessions.stream().allMatch(it -> it.getClassId().equals(testClassId)));
    }

    @Test
    void shouldFindSessionsByDelegatedStudent() {
        UUID classLeaderId = UUID.randomUUID();
        AttendanceSession session2 = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            testTeacherId
        );
        session2.delegateTo(classLeaderId);
        entityManager.persistAndFlush(session2);

        List<AttendanceSession> sessions = attendanceSessionRepository.findByDelegatedTo(classLeaderId);

        assertFalse(sessions.isEmpty());
        assertTrue(sessions.stream().allMatch(it -> it.getDelegatedTo().equals(classLeaderId)));
    }

    @Test
    void shouldFindSessionsByCreatorTeacher() {
        AttendanceSession session2 = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            testTeacherId
        );
        session2.delegateTo(UUID.randomUUID());
        entityManager.persistAndFlush(session2);

        List<AttendanceSession> sessions = attendanceSessionRepository.findByCreatedBy(testTeacherId);

        assertTrue(sessions.size() >= 2);
        assertTrue(sessions.stream().allMatch(it -> it.getCreatedBy().equals(testTeacherId)));
    }

    @Test
    void shouldFindSessionsByStatus() {
        AttendanceSession collectedSession = new AttendanceSession(
            UUID.randomUUID(),
            LocalDate.now(),
            testTeacherId
        );
        collectedSession.delegateTo(UUID.randomUUID());
        collectedSession.markAsCollected(); // Sets status to COLLECTED
        entityManager.persistAndFlush(collectedSession);

        List<AttendanceSession> collected = attendanceSessionRepository.findByStatus(AttendanceSessionStatus.COLLECTED);
        List<AttendanceSession> pending = attendanceSessionRepository.findByStatus(AttendanceSessionStatus.PENDING);

        assertFalse(collected.isEmpty());
        assertTrue(collected.stream().allMatch(it -> it.getStatus() == AttendanceSessionStatus.COLLECTED));
        assertFalse(pending.isEmpty());
        assertTrue(pending.stream().allMatch(it -> it.getStatus() == AttendanceSessionStatus.PENDING));
    }

    @Test
    void shouldFindSessionsByClassAndStatus() {
        AttendanceSession collectedSession = new AttendanceSession(
            testClassId,
            LocalDate.now().minusDays(1),
            testTeacherId
        );
        collectedSession.delegateTo(UUID.randomUUID());
        collectedSession.markAsCollected();
        entityManager.persistAndFlush(collectedSession);

        List<AttendanceSession> collected = attendanceSessionRepository.findByClassIdAndStatus(
            testClassId,
            AttendanceSessionStatus.COLLECTED
        );

        assertFalse(collected.isEmpty());
        assertTrue(collected.stream().allMatch(it -> 
            it.getClassId().equals(testClassId) && 
            it.getStatus() == AttendanceSessionStatus.COLLECTED 
        ));
    }
}
