package com.visor.school.attendance.service;

import com.visor.school.attendance.event.AttendanceEventPublisher;
import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceSessionStatus;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.repository.AttendanceRepository;
import com.visor.school.attendance.repository.AttendanceSessionRepository;
import com.visor.school.attendance.service.AttendanceCalculator.AttendanceRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Attendance service with support for direct marking and class leader delegation
 */
@Service
@Transactional
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceEventPublisher attendanceEventPublisher;
    private final RestTemplate restTemplate;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceSessionRepository attendanceSessionRepository,
            AttendanceEventPublisher attendanceEventPublisher,
            @Autowired(required = false) RestTemplate restTemplate) {
        this.attendanceRepository = attendanceRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceEventPublisher = attendanceEventPublisher;
        this.restTemplate = restTemplate;
    }

    /**
     * Mark attendance directly (teacher marks attendance without delegation)
     */
    public AttendanceRecord markAttendanceDirectly(
            UUID studentId,
            UUID classId,
            LocalDate date,
            AttendanceStatus status,
            UUID markedBy, // Teacher ID
            String notes) {
        logger.info("Marking attendance directly: student={}, class={}, date={}, status={}", 
            studentId, classId, date, status);

        // Check if record already exists
        Optional<AttendanceRecord> existing = attendanceRepository.findByStudentIdAndClassIdAndDate(studentId, classId, date);
        if (existing.isPresent()) {
            AttendanceRecord record = existing.get();
            record.updateStatus(status, markedBy, notes);
            logger.info("Updated existing attendance record: {}", record.getId());
            return attendanceRepository.save(record);
        }

        AttendanceRecord record = new AttendanceRecord(
            studentId,
            classId,
            date,
            status,
            markedBy,
            null,
            null,
            null,
            notes
        );

        AttendanceRecord saved = attendanceRepository.save(record);
        logger.info("Attendance marked directly: {}", saved.getId());

        // Publish event
        attendanceEventPublisher.publishAttendanceMarked(saved);

        return saved;
    }

    /**
     * Create attendance session for delegation
     */
    public AttendanceSession createSession(UUID classId, LocalDate date, UUID createdBy) {
        logger.info("Creating attendance session: class={}, date={}", classId, date);

        // Check if session already exists
        Optional<AttendanceSession> existing = attendanceSessionRepository.findByClassIdAndDate(classId, date);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Attendance session already exists for class " + classId + " on date " + date);
        }

        AttendanceSession session = new AttendanceSession(classId, date, createdBy);
        AttendanceSession saved = attendanceSessionRepository.save(session);
        logger.info("Attendance session created: {}", saved.getId());

        return saved;
    }

    /**
     * Delegate session to class leader
     */
    public AttendanceSession delegateToClassLeader(UUID sessionId, UUID classLeaderId, UUID classId) {
        logger.info("Delegating session {} to class leader {}", sessionId, classLeaderId);

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        if (!session.getClassId().equals(classId)) {
            throw new IllegalArgumentException("Session class ID does not match provided class ID");
        }

        // Validate class leader assignment via Academic Service API
        // In production, this would call: GET /api/v1/classes/{classId}/leaders/{classLeaderId}
        // For now, we'll validate that the session is in correct state
        if (session.getStatus() != AttendanceSessionStatus.PENDING) {
            throw new IllegalArgumentException("Session must be in PENDING status to be delegated");
        }

        // Delegate to class leader
        session.delegateTo(classLeaderId);
        AttendanceSession saved = attendanceSessionRepository.save(session);

        logger.info("Session delegated to class leader: {}", classLeaderId);

        // Publish event
        attendanceEventPublisher.publishSessionDelegated(saved, classLeaderId);

        return saved;
    }

    /**
     * Collect attendance by class leader
     */
    public AttendanceSession collectAttendanceByClassLeader(
            UUID sessionId,
            UUID classLeaderId,
            List<AttendanceEntry> attendanceEntries) {
        logger.info("Class leader {} collecting attendance for session {}", classLeaderId, sessionId);

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        if (session.getStatus() != AttendanceSessionStatus.PENDING && 
            session.getStatus() != AttendanceSessionStatus.REJECTED) {
            throw new IllegalArgumentException("Session must be in PENDING or REJECTED status to collect attendance");
        }

        if (!classLeaderId.equals(session.getDelegatedTo())) {
            throw new IllegalArgumentException("Session is not delegated to class leader " + classLeaderId);
        }

        // Validate class leader assignment via Academic Service API
        // In production, this would call: GET /api/v1/classes/{classId}/leaders/{classLeaderId}

        // Create attendance records for each entry
        for (AttendanceEntry entry : attendanceEntries) {
            Optional<AttendanceRecord> existing = attendanceRepository.findByStudentIdAndClassIdAndDate(
                entry.getStudentId(),
                session.getClassId(),
                session.getDate()
            );

            if (existing.isPresent()) {
                AttendanceRecord record = existing.get();
                record.updateStatus(entry.getStatus(), classLeaderId, entry.getNotes());
                attendanceRepository.save(record);
            } else {
                AttendanceRecord record = new AttendanceRecord(
                    entry.getStudentId(),
                    session.getClassId(),
                    session.getDate(),
                    entry.getStatus(),
                    null,
                    classLeaderId,
                    sessionId,
                    null,
                    entry.getNotes()
                );
                attendanceRepository.save(record);
            }
        }

        // Mark session as collected
        session.markAsCollected();
        AttendanceSession saved = attendanceSessionRepository.save(session);

        logger.info("Attendance collected by class leader: {}", saved.getId());

        // Publish event
        attendanceEventPublisher.publishSessionCollected(session);

        return saved;
    }

    /**
     * Approve attendance session
     */
    public AttendanceSession approveSession(UUID sessionId, UUID teacherId) {
        logger.info("Teacher {} approving session {}", teacherId, sessionId);

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        session.approve(teacherId);
        AttendanceSession saved = attendanceSessionRepository.save(session);

        // Note: Attendance records are linked via sessionId
        // The approvedBy is already set on the session
        // Records will reference the session which has approvedBy
        logger.debug("Attendance records for session {} are now approved", sessionId);

        logger.info("Session approved: {}", saved.getId());

        // Publish event
        attendanceEventPublisher.publishSessionApproved(session, teacherId);

        return saved;
    }

    /**
     * Reject attendance session
     */
    public AttendanceSession rejectSession(UUID sessionId, UUID teacherId, String reason) {
        logger.info("Teacher {} rejecting session {}: {}", teacherId, sessionId, reason);

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Attendance session not found: " + sessionId));

        session.reject(teacherId, reason);
        AttendanceSession saved = attendanceSessionRepository.save(session);

        logger.info("Session rejected: {}", saved.getId());

        // Publish event
        attendanceEventPublisher.publishSessionRejected(session, teacherId, reason);

        return saved;
    }

    /**
     * Get attendance records for a class
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByClass(UUID classId, LocalDate date) {
        if (date != null) {
            return attendanceRepository.findByClassIdAndDate(classId, date);
        } else {
            return attendanceRepository.findByClassId(classId);
        }
    }

    /**
     * Get attendance records for a student
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByStudent(UUID studentId, UUID classId) {
        if (classId != null) {
            return attendanceRepository.findByStudentIdAndClassId(studentId, classId);
        } else {
            return attendanceRepository.findAll().stream()
                .filter(r -> r.getStudentId().equals(studentId))
                .collect(Collectors.toList());
        }
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public AttendanceSession getSessionById(UUID sessionId) {
        return attendanceSessionRepository.findById(sessionId).orElse(null);
    }

    /**
     * Get sessions for a class
     */
    @Transactional(readOnly = true)
    public List<AttendanceSession> getSessionsByClass(UUID classId) {
        return attendanceSessionRepository.findByClassId(classId);
    }

    /**
     * Generate attendance report for a class
     */
    @Transactional(readOnly = true)
    public AttendanceReport generateClassReport(UUID classId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRepository.findByClassIdAndDateRange(classId, startDate, endDate);
        AttendanceCalculator calculator = new AttendanceCalculator();
        AttendanceRate rate = calculator.calculateAttendanceRateForRange(records, startDate, endDate);

        long totalDays = startDate.datesUntil(endDate.plusDays(1)).count();

        return new AttendanceReport(classId, startDate, endDate, totalDays, rate, records);
    }

    // Inner classes
    public static class AttendanceEntry {
        private final UUID studentId;
        private final AttendanceStatus status;
        private final String notes;

        public AttendanceEntry(UUID studentId, AttendanceStatus status, String notes) {
            this.studentId = studentId;
            this.status = status;
            this.notes = notes;
        }

        public UUID getStudentId() {
            return studentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public String getNotes() {
            return notes;
        }
    }

    public static class AttendanceReport {
        private final UUID classId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final long totalDays;
        private final AttendanceRate attendanceRate;
        private final List<AttendanceRecord> records;

        public AttendanceReport(UUID classId, LocalDate startDate, LocalDate endDate,
                               long totalDays, AttendanceRate attendanceRate, List<AttendanceRecord> records) {
            this.classId = classId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalDays = totalDays;
            this.attendanceRate = attendanceRate;
            this.records = records;
        }

        public UUID getClassId() {
            return classId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public long getTotalDays() {
            return totalDays;
        }

        public AttendanceRate getAttendanceRate() {
            return attendanceRate;
        }

        public List<AttendanceRecord> getRecords() {
            return records;
        }
    }
}
