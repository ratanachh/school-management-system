package com.visor.school.attendanceservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class AttendanceRecordTest {

    @Test
    fun `should create direct marking attendance record`() {
        val record = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            status = AttendanceStatus.PRESENT,
            markedBy = UUID.randomUUID()
        )

        assertNotNull(record.id)
        assertEquals(AttendanceStatus.PRESENT, record.status)
        assertNotNull(record.markedBy)
        assertNull(record.collectedBy)
        assertNull(record.sessionId)
        assertTrue(record.isDirectMarking())
        assertFalse(record.isSessionBased())
    }

    @Test
    fun `should create session-based attendance record`() {
        val sessionId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()

        val record = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            status = AttendanceStatus.PRESENT,
            collectedBy = classLeaderId,
            sessionId = sessionId
        )

        assertNotNull(record.collectedBy)
        assertNotNull(record.sessionId)
        assertNull(record.markedBy)
        assertFalse(record.isDirectMarking())
        assertTrue(record.isSessionBased())
    }

    @Test
    fun `should throw exception for future date`() {
        assertThrows<IllegalArgumentException> {
            AttendanceRecord(
                studentId = UUID.randomUUID(),
                classId = UUID.randomUUID(),
                date = LocalDate.now().plusDays(1),
                status = AttendanceStatus.PRESENT,
                markedBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should throw exception when both markedBy and collectedBy are set`() {
        assertThrows<IllegalArgumentException> {
            AttendanceRecord(
                studentId = UUID.randomUUID(),
                classId = UUID.randomUUID(),
                date = LocalDate.now(),
                status = AttendanceStatus.PRESENT,
                markedBy = UUID.randomUUID(),
                collectedBy = UUID.randomUUID(),
                sessionId = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should throw exception when sessionId provided without collectedBy`() {
        assertThrows<IllegalArgumentException> {
            AttendanceRecord(
                studentId = UUID.randomUUID(),
                classId = UUID.randomUUID(),
                date = LocalDate.now(),
                status = AttendanceStatus.PRESENT,
                sessionId = UUID.randomUUID() // Missing collectedBy
            )
        }
    }

    @Test
    fun `should throw exception when approvedBy set without sessionId`() {
        assertThrows<IllegalArgumentException> {
            AttendanceRecord(
                studentId = UUID.randomUUID(),
                classId = UUID.randomUUID(),
                date = LocalDate.now(),
                status = AttendanceStatus.PRESENT,
                markedBy = UUID.randomUUID(),
                approvedBy = UUID.randomUUID() // Requires sessionId
            )
        }
    }

    @Test
    fun `should update status and notes`() {
        val record = AttendanceRecord(
            studentId = UUID.randomUUID(),
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            status = AttendanceStatus.PRESENT,
            markedBy = UUID.randomUUID()
        )

        val initialUpdatedAt = record.updatedAt
        Thread.sleep(10)

        record.updateStatus(AttendanceStatus.ABSENT, UUID.randomUUID(), "Student was absent")

        assertEquals(AttendanceStatus.ABSENT, record.status)
        assertEquals("Student was absent", record.notes)
        assertTrue(record.updatedAt.isAfter(initialUpdatedAt))
    }

    @Test
    fun `should accept all attendance statuses`() {
        val statuses = listOf(
            AttendanceStatus.PRESENT,
            AttendanceStatus.ABSENT,
            AttendanceStatus.LATE,
            AttendanceStatus.EXCUSED
        )

        statuses.forEach { status ->
            val record = AttendanceRecord(
                studentId = UUID.randomUUID(),
                classId = UUID.randomUUID(),
                date = LocalDate.now(),
                status = status,
                markedBy = UUID.randomUUID()
            )

            assertEquals(status, record.status)
        }
    }
}

