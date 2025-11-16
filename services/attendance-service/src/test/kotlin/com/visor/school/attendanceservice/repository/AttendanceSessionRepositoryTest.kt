package com.visor.school.attendanceservice.repository

import com.visor.school.attendanceservice.model.AttendanceSession
import com.visor.school.attendanceservice.model.AttendanceSessionStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
class AttendanceSessionRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val attendanceSessionRepository: AttendanceSessionRepository
) {

    private lateinit var testSession: AttendanceSession
    private val testClassId = UUID.randomUUID()
    private val testDate = LocalDate.now()
    private val testTeacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        testSession = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        entityManager.persistAndFlush(testSession)
    }

    @Test
    fun `should find session by class and date`() {
        val found = attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate)

        assertTrue(found.isPresent)
        assertEquals(testClassId, found.get().classId)
        assertEquals(testDate, found.get().date)
    }

    @Test
    fun `should find sessions by class ID`() {
        val session2 = AttendanceSession(
            classId = testClassId,
            date = LocalDate.now().minusDays(1),
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        entityManager.persistAndFlush(session2)

        val sessions = attendanceSessionRepository.findByClassId(testClassId)

        assertTrue(sessions.size >= 2)
        assertTrue(sessions.all { it.classId == testClassId })
    }

    @Test
    fun `should find sessions by delegated student`() {
        val classLeaderId = UUID.randomUUID()
        val session2 = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = classLeaderId,
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        entityManager.persistAndFlush(session2)

        val sessions = attendanceSessionRepository.findByDelegatedTo(classLeaderId)

        assertTrue(sessions.isNotEmpty())
        assertTrue(sessions.all { it.delegatedTo == classLeaderId })
    }

    @Test
    fun `should find sessions by creator teacher`() {
        val session2 = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        entityManager.persistAndFlush(session2)

        val sessions = attendanceSessionRepository.findByCreatedBy(testTeacherId)

        assertTrue(sessions.size >= 2)
        assertTrue(sessions.all { it.createdBy == testTeacherId })
    }

    @Test
    fun `should find sessions by status`() {
        val collectedSession = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.COLLECTED
        )
        entityManager.persistAndFlush(collectedSession)

        val collected = attendanceSessionRepository.findByStatus(AttendanceSessionStatus.COLLECTED)
        val pending = attendanceSessionRepository.findByStatus(AttendanceSessionStatus.PENDING)

        assertTrue(collected.isNotEmpty())
        assertTrue(collected.all { it.status == AttendanceSessionStatus.COLLECTED })
        assertTrue(pending.isNotEmpty())
        assertTrue(pending.all { it.status == AttendanceSessionStatus.PENDING })
    }

    @Test
    fun `should find sessions by class and status`() {
        val collectedSession = AttendanceSession(
            classId = testClassId,
            date = LocalDate.now().minusDays(1),
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.COLLECTED
        )
        entityManager.persistAndFlush(collectedSession)

        val collected = attendanceSessionRepository.findByClassIdAndStatus(
            testClassId,
            AttendanceSessionStatus.COLLECTED
        )

        assertTrue(collected.isNotEmpty())
        assertTrue(collected.all { 
            it.classId == testClassId && 
            it.status == AttendanceSessionStatus.COLLECTED 
        })
    }
}

