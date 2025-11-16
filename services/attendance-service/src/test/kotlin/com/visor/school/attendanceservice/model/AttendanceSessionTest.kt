package com.visor.school.attendanceservice.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class AttendanceSessionTest {

    @Test
    fun `should create attendance session with required fields`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID()
        )

        assertNotNull(session.id)
        assertEquals(AttendanceSessionStatus.PENDING, session.status)
        assertNotNull(session.delegatedTo)
        assertNotNull(session.createdBy)
        assertNull(session.approvedBy)
        assertNull(session.rejectedBy)
    }

    @Test
    fun `should throw exception for future date`() {
        assertThrows<IllegalArgumentException> {
            AttendanceSession(
                classId = UUID.randomUUID(),
                date = LocalDate.now().plusDays(1),
                delegatedTo = UUID.randomUUID(),
                createdBy = UUID.randomUUID()
            )
        }
    }

    @Test
    fun `should mark session as collected`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.PENDING
        )

        session.markAsCollected()

        assertEquals(AttendanceSessionStatus.COLLECTED, session.status)
        assertNotNull(session.collectedAt)
    }

    @Test
    fun `should throw exception when marking collected without delegation`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.APPROVED
        )

        assertThrows<IllegalArgumentException> {
            session.markAsCollected()
        }
    }

    @Test
    fun `should approve session`() {
        val teacherId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.COLLECTED
        )
        session.markAsCollected()

        session.approve(teacherId)

        assertEquals(AttendanceSessionStatus.APPROVED, session.status)
        assertEquals(teacherId, session.approvedBy)
        assertNotNull(session.approvedAt)
        assertNull(session.rejectedBy)
        assertNull(session.rejectionReason)
    }

    @Test
    fun `should throw exception when approving non-collected session`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.PENDING
        )

        assertThrows<IllegalArgumentException> {
            session.approve(UUID.randomUUID())
        }
    }

    @Test
    fun `should reject session with reason`() {
        val teacherId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.COLLECTED
        )
        session.markAsCollected()

        session.reject(teacherId, "Incomplete attendance data")

        assertEquals(AttendanceSessionStatus.REJECTED, session.status)
        assertEquals(teacherId, session.rejectedBy)
        assertEquals("Incomplete attendance data", session.rejectionReason)
        assertNotNull(session.rejectedAt)
        assertNull(session.collectedAt) // Reset after rejection
    }

    @Test
    fun `should throw exception when rejecting without reason`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.COLLECTED
        )
        session.markAsCollected()

        assertThrows<IllegalArgumentException> {
            session.reject(UUID.randomUUID(), "")
        }
    }

    @Test
    fun `should resubmit rejected session`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.REJECTED
        )
        session.reject(UUID.randomUUID(), "Incomplete")

        session.resubmit()

        assertEquals(AttendanceSessionStatus.COLLECTED, session.status)
        assertNotNull(session.collectedAt)
        assertNull(session.rejectionReason)
        assertNull(session.rejectedBy)
    }

    @Test
    fun `should throw exception when resubmitting non-rejected session`() {
        val session = AttendanceSession(
            classId = UUID.randomUUID(),
            date = LocalDate.now(),
            delegatedTo = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            status = AttendanceSessionStatus.PENDING
        )

        assertThrows<IllegalArgumentException> {
            session.resubmit()
        }
    }
}

