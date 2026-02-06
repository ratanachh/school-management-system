package com.visor.school.academic.service;

import com.visor.school.academic.model.LeadershipPosition;
import com.visor.school.academic.model.StudentClassLeadership;
import com.visor.school.academic.repository.StudentClassLeadershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing student class leadership assignments
 * Validates: Only one student per position per class
 */
@Service
@Transactional
public class StudentClassLeadershipService {
    private static final Logger logger = LoggerFactory.getLogger(StudentClassLeadershipService.class);

    private final StudentClassLeadershipRepository leadershipRepository;

    public StudentClassLeadershipService(StudentClassLeadershipRepository leadershipRepository) {
        this.leadershipRepository = leadershipRepository;
    }

    /**
     * Assign a class leader to a class
     * Validates: Only one 1st leader, one 2nd leader, one 3rd leader per class
     */
    public StudentClassLeadership assignLeader(
            UUID studentId,
            UUID classId,
            LeadershipPosition position,
            UUID assignedBy
    ) {
        logger.info("Assigning class leader: student={}, class={}, position={}", studentId, classId, position);

        if (position == LeadershipPosition.NONE) {
            throw new IllegalArgumentException("Cannot assign NONE position");
        }

        // Check if position is already assigned
        if (leadershipRepository.existsByClassIdAndLeadershipPosition(classId, position)) {
            StudentClassLeadership existing = leadershipRepository.findByClassIdAndPosition(classId, position).orElse(null);
            throw new IllegalArgumentException(
                    "Position " + position + " is already assigned to student " + 
                    (existing != null ? existing.getStudentId() : "unknown") + " in class " + classId
            );
        }

        // Check if student already has a position in this class
        if (leadershipRepository.existsByStudentIdAndClassId(studentId, classId)) {
            throw new IllegalArgumentException(
                    "Student " + studentId + " already has a leadership position in class " + classId
            );
        }

        StudentClassLeadership leadership = new StudentClassLeadership(
                studentId,
                classId,
                position,
                assignedBy
        );

        StudentClassLeadership saved = leadershipRepository.save(leadership);
        logger.info("Class leader assigned: {}", saved.getId());

        return saved;
    }

    /**
     * Get all class leaders for a class
     */
    @Transactional(readOnly = true)
    public List<StudentClassLeadership> getLeadersByClass(UUID classId) {
        return leadershipRepository.findByClassId(classId);
    }

    /**
     * Get class leader by position
     */
    @Transactional(readOnly = true)
    public StudentClassLeadership getLeaderByPosition(UUID classId, LeadershipPosition position) {
        return leadershipRepository.findByClassIdAndPosition(classId, position).orElse(null);
    }

    /**
     * Check if student is a class leader for the class
     */
    @Transactional(readOnly = true)
    public boolean isClassLeader(UUID studentId, UUID classId) {
        return leadershipRepository.findByStudentIdAndClassId(studentId, classId).isPresent();
    }

    /**
     * Remove class leader assignment
     */
    public void removeLeader(UUID studentId, UUID classId) {
        logger.info("Removing class leader: student={}, class={}", studentId, classId);

        StudentClassLeadership leadership = leadershipRepository.findByStudentIdAndClassId(studentId, classId)
                .orElseThrow(() -> new IllegalArgumentException("Class leader assignment not found"));

        leadershipRepository.delete(leadership);
        logger.info("Class leader assignment removed");
    }
}
