package com.visor.school.attendanceservice.repository

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceStatus
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
class AttendanceRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val attendanceRepository: AttendanceRepository
) {

    private lateinit var testRecord: AttendanceRecord
    private val testStudentId = UUID.randomUUID()
    private val testClassId = UUID.randomUUID()
    private val testDate = LocalDate.now()

    @BeforeEach
    fun setup() {
        testRecord = AttendanceRecord(
            studentId = testStudentId,
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.PRESENT,
            markedBy = UUID.randomUUID()
        )
        entityManager.persistAndFlush(testRecord)
    }

    @Test
    fun `should find attendance by student, class and date`() {
        val found = attendanceRepository.findByStudentIdAndClassIdAndDate(
            testStudentId,
            testClassId,
            testDate
        )

        assertTrue(found.isPresent)
        assertEquals(testStudentId, found.get().studentId)
        assertEquals(testClassId, found.get().classId)
        assertEquals(testDate, found.get().date)
    }

    @Test
    fun `should find attendance records by class and date`() {
        val record2 = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.ABSENT,
            markedBy = UUID.randomUUID()
        )
        entityManager.persistAndFlush(record2)

        val records = attendanceRepository.findByClassIdAndDate(testClassId, testDate)

        assertTrue(records.size >= 2)
        assertTrue(records.all { it.classId == testClassId && it.date == testDate })
    }

    @Test
    fun `should find attendance records by student and class`() {
        val record2 = AttendanceRecord(
            studentId = testStudentId,
            classId = testClassId,
            date = LocalDate.now().minusDays(1),
            status = AttendanceStatus.LATE,
            markedBy = UUID.randomUUID()
        )
        entityManager.persistAndFlush(record2)

        val records = attendanceRepository.findByStudentIdAndClassId(testStudentId, testClassId)

        assertTrue(records.size >= 2)
        assertTrue(records.all { it.studentId == testStudentId && it.classId == testClassId })
    }

    @Test
    fun `should find attendance records by session ID`() {
        val sessionId = UUID.randomUUID()
        val sessionRecord = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.PRESENT,
            collectedBy = UUID.randomUUID(),
            sessionId = sessionId
        )
        entityManager.persistAndFlush(sessionRecord)

        val records = attendanceRepository.findBySessionId(sessionId)

        assertTrue(records.isNotEmpty())
        assertTrue(records.all { it.sessionId == sessionId })
    }

    @Test
    fun `should find attendance records by date range`() {
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()

        val record2 = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = testClassId,
            date = startDate,
            status = AttendanceStatus.PRESENT,
            markedBy = UUID.randomUUID()
        )
        entityManager.persistAndFlush(record2)

        val records = attendanceRepository.findByClassIdAndDateRange(testClassId, startDate, endDate)

        assertTrue(records.isNotEmpty())
        assertTrue(records.all { 
            it.classId == testClassId && 
            it.date.isAfter(startDate.minusDays(1)) && 
            it.date.isBefore(endDate.plusDays(1))
        })
    }

    @Test
    fun `should count attendance by status`() {
        val absentRecord = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.ABSENT,
            markedBy = UUID.randomUUID()
        )
        entityManager.persistAndFlush(absentRecord)

        val presentCount = attendanceRepository.countByClassIdAndDateAndStatus(
            testClassId,
            testDate,
            AttendanceStatus.PRESENT
        )
        val absentCount = attendanceRepository.countByClassIdAndDateAndStatus(
            testClassId,
            testDate,
            AttendanceStatus.ABSENT
        )

        assertTrue(presentCount >= 1)
        assertTrue(absentCount >= 1)
    }
}

