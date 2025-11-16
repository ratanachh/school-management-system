package com.visor.school.attendanceservice.integration

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceSession
import com.visor.school.attendanceservice.model.AttendanceSessionStatus
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.repository.AttendanceRepository
import com.visor.school.attendanceservice.repository.AttendanceSessionRepository
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
 * Integration test for class leader attendance collection and approval flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassLeaderAttendanceIntegrationTest @Autowired constructor(
    private val attendanceService: AttendanceService,
    private val attendanceSessionRepository: AttendanceSessionRepository,
    private val attendanceRepository: AttendanceRepository
) {

    @Test
    fun `should complete class leader attendance collection and approval flow`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()
        val studentId1 = UUID.randomUUID()
        val studentId2 = UUID.randomUUID()
        val date = LocalDate.now()

        // Step 1: Teacher creates session
        val session = attendanceService.createSession(
            classId = classId,
            date = date,
            createdBy = teacherId
        )

        assertEquals(AttendanceSessionStatus.PENDING, session.status)
        assertNull(session.delegatedTo)

        // Step 2: Teacher delegates to class leader
        val delegatedSession = attendanceService.delegateToClassLeader(
            sessionId = session.id,
            classLeaderId = classLeaderId,
            classId = classId
        )

        assertEquals(classLeaderId, delegatedSession.delegatedTo)
        assertEquals(AttendanceSessionStatus.PENDING, delegatedSession.status)

        // Step 3: Class leader collects attendance
        val attendanceEntries = listOf(
            AttendanceEntry(studentId1, AttendanceStatus.PRESENT, null),
            AttendanceEntry(studentId2, AttendanceStatus.ABSENT, "Student absent")
        )

        val collectedSession = attendanceService.collectAttendanceByClassLeader(
            sessionId = session.id,
            classLeaderId = classLeaderId,
            attendanceEntries = attendanceEntries
        )

        assertEquals(AttendanceSessionStatus.COLLECTED, collectedSession.status)
        assertNotNull(collectedSession.collectedAt)

        // Verify attendance records created
        val records = attendanceRepository.findBySessionId(session.id)
        assertEquals(2, records.size)
        assertTrue(records.any { it.studentId == studentId1 && it.status == AttendanceStatus.PRESENT })
        assertTrue(records.any { it.studentId == studentId2 && it.status == AttendanceStatus.ABSENT })

        // Step 4: Teacher approves session
        val approvedSession = attendanceService.approveSession(session.id, teacherId)

        assertEquals(AttendanceSessionStatus.APPROVED, approvedSession.status)
        assertEquals(teacherId, approvedSession.approvedBy)
        assertNotNull(approvedSession.approvedAt)

        // Verify records are linked to session
        val approvedRecords = attendanceRepository.findBySessionId(session.id)
        assertTrue(approvedRecords.all { it.sessionId == session.id })
    }

    @Test
    fun `should handle session rejection and resubmission`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val date = LocalDate.now()

        // Step 1: Create and delegate session
        val session = attendanceService.createSession(classId, date, teacherId)
        attendanceService.delegateToClassLeader(session.id, classLeaderId, classId)

        // Step 2: Class leader collects
        val collectedSession = attendanceService.collectAttendanceByClassLeader(
            sessionId = session.id,
            classLeaderId = classLeaderId,
            attendanceEntries = listOf(AttendanceEntry(studentId, AttendanceStatus.PRESENT))
        )

        assertEquals(AttendanceSessionStatus.COLLECTED, collectedSession.status)

        // Step 3: Teacher rejects
        val rejectedSession = attendanceService.rejectSession(
            sessionId = session.id,
            teacherId = teacherId,
            reason = "Incomplete attendance data"
        )

        assertEquals(AttendanceSessionStatus.REJECTED, rejectedSession.status)
        assertEquals("Incomplete attendance data", rejectedSession.rejectionReason)

        // Step 4: Class leader resubmits
        val sessionEntity = attendanceSessionRepository.findById(session.id).get()
        sessionEntity.resubmit()
        attendanceSessionRepository.save(sessionEntity)

        val resubmitted = attendanceSessionRepository.findById(session.id).get()
        assertEquals(AttendanceSessionStatus.COLLECTED, resubmitted.status)
        assertNull(resubmitted.rejectionReason)
    }

    @Test
    fun `should prevent collection by non-delegated leader`() {
        // Given
        val classId = UUID.randomUUID()
        val teacherId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()
        val otherStudentId = UUID.randomUUID()
        val date = LocalDate.now()

        val session = attendanceService.createSession(classId, date, teacherId)
        attendanceService.delegateToClassLeader(session.id, classLeaderId, classId)

        // When & Then
        assertThrows<IllegalArgumentException> {
            attendanceService.collectAttendanceByClassLeader(
                sessionId = session.id,
                classLeaderId = otherStudentId, // Not the delegated leader
                attendanceEntries = listOf(AttendanceEntry(UUID.randomUUID(), AttendanceStatus.PRESENT))
            )
        }
    }
}

// Data class for attendance entry
data class AttendanceEntry(
    val studentId: UUID,
    val status: AttendanceStatus,
    val notes: String? = null
)

