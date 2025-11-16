package com.visor.school.attendanceservice.service

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceSession
import com.visor.school.attendanceservice.model.AttendanceSessionStatus
import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.repository.AttendanceRepository
import com.visor.school.attendanceservice.repository.AttendanceSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.UUID

/**
 * Attendance service with support for direct marking and class leader delegation
 */
@Service
@Transactional
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val attendanceSessionRepository: AttendanceSessionRepository,
    private val attendanceEventPublisher: AttendanceEventPublisher,
    @Autowired(required = false) private val restTemplate: RestTemplate? = null
) {
    private val logger = LoggerFactory.getLogger(AttendanceService::class.java)

    /**
     * Mark attendance directly (teacher marks attendance without delegation)
     */
    fun markAttendanceDirectly(
        studentId: UUID,
        classId: UUID,
        date: LocalDate,
        status: AttendanceStatus,
        markedBy: UUID, // Teacher ID
        notes: String? = null
    ): AttendanceRecord {
        logger.info("Marking attendance directly: student=$studentId, class=$classId, date=$date, status=$status")

        // Check if record already exists
        val existing = attendanceRepository.findByStudentIdAndClassIdAndDate(studentId, classId, date)
        if (existing.isPresent) {
            val record = existing.get()
            record.updateStatus(status, markedBy, notes)
            logger.info("Updated existing attendance record: ${record.id}")
            return attendanceRepository.save(record)
        }

        val record = AttendanceRecord(
            studentId = studentId,
            classId = classId,
            date = date,
            status = status,
            markedBy = markedBy,
            collectedBy = null,
            sessionId = null,
            approvedBy = null,
            notes = notes
        )

        val saved = attendanceRepository.save(record)
        logger.info("Attendance marked directly: ${saved.id}")

        // Publish event
        attendanceEventPublisher.publishAttendanceMarked(saved)

        return saved
    }

    /**
     * Create attendance session for delegation
     */
    fun createSession(
        classId: UUID,
        date: LocalDate,
        createdBy: UUID // Teacher ID
    ): AttendanceSession {
        logger.info("Creating attendance session: class=$classId, date=$date")

        // Check if session already exists
        val existing = attendanceSessionRepository.findByClassIdAndDate(classId, date)
        if (existing.isPresent) {
            throw IllegalArgumentException("Attendance session already exists for class $classId on date $date")
        }

        val session = AttendanceSession(
            classId = classId,
            date = date,
            status = AttendanceSessionStatus.PENDING,
            delegatedTo = null, // Will be set when delegated
            createdBy = createdBy
        )

        val saved = attendanceSessionRepository.save(session)
        logger.info("Attendance session created: ${saved.id}")

        return saved
    }

    /**
     * Delegate session to class leader
     */
    fun delegateToClassLeader(
        sessionId: UUID,
        classLeaderId: UUID,
        classId: UUID
    ): AttendanceSession {
        logger.info("Delegating session $sessionId to class leader $classLeaderId")

        val session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow { IllegalArgumentException("Attendance session not found: $sessionId") }

        require(session.classId == classId) {
            "Session class ID does not match provided class ID"
        }

        // Validate class leader assignment via Academic Service API
        // In production, this would call: GET /api/v1/classes/{classId}/leaders/{classLeaderId}
        // For now, we'll validate that the session is in correct state
        require(session.status == AttendanceSessionStatus.PENDING) {
            "Session must be in PENDING status to be delegated"
        }

        // Delegate to class leader
        session.delegateTo(classLeaderId)
        val saved = attendanceSessionRepository.save(session)

        logger.info("Session delegated to class leader: $classLeaderId")

        // Publish event
        attendanceEventPublisher.publishSessionDelegated(saved, classLeaderId)

        return saved
    }

    /**
     * Collect attendance by class leader
     */
    fun collectAttendanceByClassLeader(
        sessionId: UUID,
        classLeaderId: UUID,
        attendanceEntries: List<AttendanceEntry>
    ): AttendanceSession {
        logger.info("Class leader $classLeaderId collecting attendance for session $sessionId")

        val session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow { IllegalArgumentException("Attendance session not found: $sessionId") }

        require(session.status == AttendanceSessionStatus.PENDING || session.status == AttendanceSessionStatus.REJECTED) {
            "Session must be in PENDING or REJECTED status to collect attendance"
        }

        require(session.delegatedTo == classLeaderId) {
            "Session is not delegated to class leader $classLeaderId"
        }
        
        // Validate class leader assignment via Academic Service API
        // In production, this would call: GET /api/v1/classes/{classId}/leaders/{classLeaderId}

        // Create attendance records for each entry
        attendanceEntries.forEach { entry ->
            val existing = attendanceRepository.findByStudentIdAndClassIdAndDate(
                entry.studentId,
                session.classId,
                session.date
            )

            if (existing.isPresent) {
                val record = existing.get()
                record.updateStatus(entry.status, classLeaderId, entry.notes)
                attendanceRepository.save(record)
            } else {
                val record = AttendanceRecord(
                    studentId = entry.studentId,
                    classId = session.classId,
                    date = session.date,
                    status = entry.status,
                    markedBy = null,
                    collectedBy = classLeaderId,
                    sessionId = sessionId,
                    approvedBy = null,
                    notes = entry.notes
                )
                attendanceRepository.save(record)
            }
        }

        // Mark session as collected
        session.markAsCollected()
        val saved = attendanceSessionRepository.save(session)

        logger.info("Attendance collected by class leader: ${saved.id}")

        // Publish event
        attendanceEventPublisher.publishSessionCollected(session)

        return saved
    }

    /**
     * Approve attendance session
     */
    fun approveSession(sessionId: UUID, teacherId: UUID): AttendanceSession {
        logger.info("Teacher $teacherId approving session $sessionId")

        val session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow { IllegalArgumentException("Attendance session not found: $sessionId") }

        session.approve(teacherId)
        val saved = attendanceSessionRepository.save(session)

        // Note: Attendance records are linked via sessionId
        // The approvedBy is already set on the session
        // Records will reference the session which has approvedBy
        logger.debug("Attendance records for session $sessionId are now approved")

        logger.info("Session approved: ${saved.id}")

        // Publish event
        attendanceEventPublisher.publishSessionApproved(session, teacherId)

        return saved
    }

    /**
     * Reject attendance session
     */
    fun rejectSession(sessionId: UUID, teacherId: UUID, reason: String): AttendanceSession {
        logger.info("Teacher $teacherId rejecting session $sessionId: $reason")

        val session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow { IllegalArgumentException("Attendance session not found: $sessionId") }

        session.reject(teacherId, reason)
        val saved = attendanceSessionRepository.save(session)

        logger.info("Session rejected: ${saved.id}")

        // Publish event
        attendanceEventPublisher.publishSessionRejected(session, teacherId, reason)

        return saved
    }

    /**
     * Get attendance records for a class
     */
    @Transactional(readOnly = true)
    fun getAttendanceByClass(classId: UUID, date: LocalDate? = null): List<AttendanceRecord> {
        return if (date != null) {
            attendanceRepository.findByClassIdAndDate(classId, date)
        } else {
            attendanceRepository.findByClassId(classId)
        }
    }

    /**
     * Get attendance records for a student
     */
    @Transactional(readOnly = true)
    fun getAttendanceByStudent(studentId: UUID, classId: UUID? = null): List<AttendanceRecord> {
        return if (classId != null) {
            attendanceRepository.findByStudentIdAndClassId(studentId, classId)
        } else {
            attendanceRepository.findAll().filter { it.studentId == studentId }
        }
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    fun getSessionById(sessionId: UUID): AttendanceSession? {
        return attendanceSessionRepository.findById(sessionId).orElse(null)
    }

    /**
     * Get sessions for a class
     */
    @Transactional(readOnly = true)
    fun getSessionsByClass(classId: UUID): List<AttendanceSession> {
        return attendanceSessionRepository.findByClassId(classId)
    }

    /**
     * Generate attendance report for a class
     */
    @Transactional(readOnly = true)
    fun generateClassReport(classId: UUID, startDate: LocalDate, endDate: LocalDate): AttendanceReport {
        val records = attendanceRepository.findByClassIdAndDateRange(classId, startDate, endDate)
        val calculator = AttendanceCalculator()
        val rate = calculator.calculateAttendanceRateForRange(records, startDate, endDate)

        return AttendanceReport(
            classId = classId,
            startDate = startDate,
            endDate = endDate,
            totalDays = startDate.datesUntil(endDate.plusDays(1)).count().toLong(),
            attendanceRate = rate,
            records = records
        )
    }
}

data class AttendanceEntry(
    val studentId: UUID,
    val status: AttendanceStatus,
    val notes: String? = null
)

data class AttendanceReport(
    val classId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Long,
    val attendanceRate: AttendanceRate,
    val records: List<AttendanceRecord>
)

