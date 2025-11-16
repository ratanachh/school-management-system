package com.visor.school.attendanceservice.service

import com.visor.school.attendanceservice.model.*
import com.visor.school.attendanceservice.repository.AttendanceRepository
import com.visor.school.attendanceservice.repository.AttendanceSessionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AttendanceServiceTest {

    @Mock
    private lateinit var attendanceRepository: AttendanceRepository

    @Mock
    private lateinit var attendanceSessionRepository: AttendanceSessionRepository

    @Mock
    private lateinit var attendanceEventPublisher: AttendanceEventPublisher

    @InjectMocks
    private lateinit var attendanceService: AttendanceService

    private val testStudentId = UUID.randomUUID()
    private val testClassId = UUID.randomUUID()
    private val testTeacherId = UUID.randomUUID()
    private val testDate = LocalDate.now()

    @BeforeEach
    fun setup() {
        attendanceService = AttendanceService(
            attendanceRepository,
            attendanceSessionRepository,
            attendanceEventPublisher,
            null
        )
    }

    @Test
    fun `should mark attendance directly`() {
        // Given
        whenever(attendanceRepository.findByStudentIdAndClassIdAndDate(testStudentId, testClassId, testDate))
            .thenReturn(Optional.empty())
        whenever(attendanceRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceRecord }
        doNothing().whenever(attendanceEventPublisher).publishAttendanceMarked(any())

        // When
        val result = attendanceService.markAttendanceDirectly(
            studentId = testStudentId,
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.PRESENT,
            markedBy = testTeacherId
        )

        // Then
        assertNotNull(result)
        assertEquals(testStudentId, result.studentId)
        assertEquals(AttendanceStatus.PRESENT, result.status)
        assertEquals(testTeacherId, result.markedBy)
        assertTrue(result.isDirectMarking())
        verify(attendanceRepository).save(any())
        verify(attendanceEventPublisher).publishAttendanceMarked(any())
    }

    @Test
    fun `should update existing attendance record`() {
        // Given
        val existingRecord = AttendanceRecord(
            studentId = testStudentId,
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.PRESENT,
            markedBy = testTeacherId
        )
        whenever(attendanceRepository.findByStudentIdAndClassIdAndDate(testStudentId, testClassId, testDate))
            .thenReturn(Optional.of(existingRecord))
        whenever(attendanceRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceRecord }

        // When
        val result = attendanceService.markAttendanceDirectly(
            studentId = testStudentId,
            classId = testClassId,
            date = testDate,
            status = AttendanceStatus.ABSENT,
            markedBy = testTeacherId
        )

        // Then
        assertEquals(AttendanceStatus.ABSENT, result.status)
        verify(attendanceRepository).save(existingRecord)
    }

    @Test
    fun `should create attendance session`() {
        // Given
        whenever(attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate))
            .thenReturn(Optional.empty())
        whenever(attendanceSessionRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceSession }

        // When
        val result = attendanceService.createSession(
            classId = testClassId,
            date = testDate,
            createdBy = testTeacherId
        )

        // Then
        assertNotNull(result)
        assertEquals(testClassId, result.classId)
        assertEquals(testDate, result.date)
        assertEquals(AttendanceSessionStatus.PENDING, result.status)
        verify(attendanceSessionRepository).save(any())
    }

    @Test
    fun `should throw exception when session already exists`() {
        // Given
        val existingSession = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId
        )
        whenever(attendanceSessionRepository.findByClassIdAndDate(testClassId, testDate))
            .thenReturn(Optional.of(existingSession))

        // When & Then
        assertThrows<IllegalArgumentException> {
            attendanceService.createSession(testClassId, testDate, testTeacherId)
        }
    }

    @Test
    fun `should delegate session to class leader`() {
        // Given
        val sessionId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = null,
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        whenever(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        whenever(attendanceSessionRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceSession }
        doNothing().whenever(attendanceEventPublisher).publishSessionDelegated(any(), any())

        // When
        val result = attendanceService.delegateToClassLeader(sessionId, classLeaderId, testClassId)

        // Then
        assertEquals(classLeaderId, result.delegatedTo)
        verify(attendanceSessionRepository).save(session)
        verify(attendanceEventPublisher).publishSessionDelegated(any(), eq(classLeaderId))
    }

    @Test
    fun `should collect attendance by class leader`() {
        // Given
        val sessionId = UUID.randomUUID()
        val classLeaderId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = classLeaderId,
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.PENDING
        )
        val attendanceEntries = listOf(
            AttendanceEntry(
                studentId = testStudentId,
                status = AttendanceStatus.PRESENT,
                notes = null
            )
        )

        whenever(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        whenever(attendanceRepository.findByStudentIdAndClassIdAndDate(any(), any(), any()))
            .thenReturn(Optional.empty())
        whenever(attendanceRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceRecord }
        whenever(attendanceSessionRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceSession }
        doNothing().whenever(attendanceEventPublisher).publishSessionCollected(any())

        // When
        val result = attendanceService.collectAttendanceByClassLeader(
            sessionId = sessionId,
            classLeaderId = classLeaderId,
            attendanceEntries = attendanceEntries
        )

        // Then
        assertEquals(AttendanceSessionStatus.COLLECTED, result.status)
        assertNotNull(result.collectedAt)
        verify(attendanceRepository).save(any())
        verify(attendanceEventPublisher).publishSessionCollected(any())
    }

    @Test
    fun `should approve session`() {
        // Given
        val sessionId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.COLLECTED
        )
        session.markAsCollected()

        whenever(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        whenever(attendanceSessionRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceSession }
        doNothing().whenever(attendanceEventPublisher).publishSessionApproved(any(), any())

        // When
        val result = attendanceService.approveSession(sessionId, testTeacherId)

        // Then
        assertEquals(AttendanceSessionStatus.APPROVED, result.status)
        assertEquals(testTeacherId, result.approvedBy)
        verify(attendanceEventPublisher).publishSessionApproved(any(), eq(testTeacherId))
    }

    @Test
    fun `should reject session with reason`() {
        // Given
        val sessionId = UUID.randomUUID()
        val session = AttendanceSession(
            classId = testClassId,
            date = testDate,
            delegatedTo = UUID.randomUUID(),
            createdBy = testTeacherId,
            status = AttendanceSessionStatus.COLLECTED
        )
        session.markAsCollected()

        whenever(attendanceSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        whenever(attendanceSessionRepository.save(any())).thenAnswer { it.arguments[0] as AttendanceSession }
        doNothing().whenever(attendanceEventPublisher).publishSessionRejected(any(), any(), any())

        // When
        val result = attendanceService.rejectSession(sessionId, testTeacherId, "Incomplete data")

        // Then
        assertEquals(AttendanceSessionStatus.REJECTED, result.status)
        assertEquals(testTeacherId, result.rejectedBy)
        assertEquals("Incomplete data", result.rejectionReason)
        verify(attendanceEventPublisher).publishSessionRejected(any(), eq(testTeacherId), eq("Incomplete data"))
    }
}

// Data class for attendance entry
data class AttendanceEntry(
    val studentId: UUID,
    val status: AttendanceStatus,
    val notes: String? = null
)

