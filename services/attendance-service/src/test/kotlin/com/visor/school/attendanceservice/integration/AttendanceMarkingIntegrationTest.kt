package com.visor.school.attendanceservice.integration

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.repository.AttendanceRepository
import com.visor.school.attendanceservice.service.AttendanceService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Integration test for direct attendance marking flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceMarkingIntegrationTest @Autowired constructor(
    private val attendanceService: AttendanceService,
    private val attendanceRepository: AttendanceRepository
) {

    @Test
    fun `should complete direct attendance marking flow`() {
        // Given
        val studentId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val date = LocalDate.now()

        // When
        val record = attendanceService.markAttendanceDirectly(
            studentId = studentId,
            classId = classId,
            date = date,
            status = AttendanceStatus.PRESENT,
            markedBy = teacherId,
            notes = "Student present"
        )

        // Then
        assertNotNull(record)
        assertNotNull(record.id)
        assertEquals(studentId, record.studentId)
        assertEquals(classId, record.classId)
        assertEquals(date, record.date)
        assertEquals(AttendanceStatus.PRESENT, record.status)
        assertEquals(teacherId, record.markedBy)
        assertTrue(record.isDirectMarking())
        assertFalse(record.isSessionBased())

        // Verify persisted
        val saved = attendanceRepository.findById(record.id)
        assertTrue(saved.isPresent)
        assertEquals(AttendanceStatus.PRESENT, saved.get().status)
    }

    @Test
    fun `should update existing attendance record`() {
        // Given
        val studentId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val date = LocalDate.now()

        attendanceService.markAttendanceDirectly(
            studentId = studentId,
            classId = classId,
            date = date,
            status = AttendanceStatus.PRESENT,
            markedBy = teacherId
        )

        // When
        val updated = attendanceService.markAttendanceDirectly(
            studentId = studentId,
            classId = classId,
            date = date,
            status = AttendanceStatus.ABSENT,
            markedBy = teacherId,
            notes = "Updated to absent"
        )

        // Then
        assertEquals(AttendanceStatus.ABSENT, updated.status)
        assertEquals("Updated to absent", updated.notes)
    }

    @Test
    fun `should mark multiple students attendance`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val date = LocalDate.now()

        // When
        val record1 = attendanceService.markAttendanceDirectly(
            studentId = UUID.randomUUID(),
            classId = classId,
            date = date,
            status = AttendanceStatus.PRESENT,
            markedBy = teacherId
        )

        val record2 = attendanceService.markAttendanceDirectly(
            studentId = UUID.randomUUID(),
            classId = classId,
            date = date,
            status = AttendanceStatus.LATE,
            markedBy = teacherId
        )

        // Then
        assertNotNull(record1)
        assertNotNull(record2)
        assertEquals(classId, record1.classId)
        assertEquals(classId, record2.classId)
        assertEquals(date, record1.date)
        assertEquals(date, record2.date)
    }

    @Test
    fun `should handle different attendance statuses`() {
        // Given
        val studentId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val date = LocalDate.now()

        val statuses = listOf(
            AttendanceStatus.PRESENT,
            AttendanceStatus.ABSENT,
            AttendanceStatus.LATE,
            AttendanceStatus.EXCUSED
        )

        // When & Then
        statuses.forEach { status ->
            val record = attendanceService.markAttendanceDirectly(
                studentId = UUID.randomUUID(),
                classId = classId,
                date = date,
                status = status,
                markedBy = teacherId
            )

            assertEquals(status, record.status)
        }
    }
}

