package com.visor.school.academicservice.service;

import com.visor.school.academicservice.event.StudentEventPublisher;
import com.visor.school.academicservice.model.Address;
import com.visor.school.academicservice.model.EmergencyContact;
import com.visor.school.academicservice.model.EnrollmentStatus;
import com.visor.school.academicservice.model.Student;
import com.visor.school.academicservice.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Student service with grade level validation (1-12 for K12 system)
 */
@Service
@Transactional
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final StudentIdGenerator studentIdGenerator;
    private final StudentEventPublisher studentEventPublisher;

    public StudentService(
            StudentRepository studentRepository,
            StudentIdGenerator studentIdGenerator,
            StudentEventPublisher studentEventPublisher
    ) {
        this.studentRepository = studentRepository;
        this.studentIdGenerator = studentIdGenerator;
        this.studentEventPublisher = studentEventPublisher;
    }

    /**
     * Enroll a new student
     * Validates grade level is between 1 and 12
     */
    public Student enrollStudent(
            UUID userId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            int gradeLevel,
            Address address,
            EmergencyContact emergencyContact
    ) {
        logger.info("Enrolling student: {} {}, grade level: {}", firstName, lastName, gradeLevel);

        // Validate grade level
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12 (K12 system), got: " + gradeLevel);
        }

        // Generate student ID
        String studentId = studentIdGenerator.generateStudentId();

        // Check if student with this user ID already exists
        if (studentRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Student with user ID " + userId + " already exists");
        }

        Student student = new Student(
                studentId,
                userId,
                firstName,
                lastName,
                dateOfBirth,
                gradeLevel,
                EnrollmentStatus.ENROLLED,
                address,
                emergencyContact
        );

        Student saved = studentRepository.save(student);
        logger.info("Student enrolled successfully: {}", saved.getStudentId());

        // Publish student enrolled event
        studentEventPublisher.publishStudentEnrolled(saved);

        return saved;
    }

    /**
     * Get student by ID
     */
    @Transactional(readOnly = true)
    public Student getStudentById(UUID id) {
        return studentRepository.findById(id).orElse(null);
    }

    /**
     * Get student by student ID
     */
    @Transactional(readOnly = true)
    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId).orElse(null);
    }

    /**
     * Get student by user ID
     */
    @Transactional(readOnly = true)
    public Student getStudentByUserId(UUID userId) {
        return studentRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Search students by name
     */
    @Transactional(readOnly = true)
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.searchByName(name);
    }

    /**
     * Get students by grade level
     */
    @Transactional(readOnly = true)
    public List<Student> getStudentsByGradeLevel(int gradeLevel) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12, got: " + gradeLevel);
        }
        return studentRepository.findByGradeLevel(gradeLevel);
    }

    /**
     * Update student information
     */
    public Student updateStudent(
            UUID id,
            String firstName,
            String lastName,
            Integer gradeLevel,
            Address address,
            EmergencyContact emergencyContact
    ) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        if (firstName != null) {
            // Note: In a full implementation, we'd update the entity fields
            // For now, we'll just update the timestamp
            logger.info("Updating student {}: firstName", id);
        }

        if (lastName != null) {
            logger.info("Updating student {}: lastName", id);
        }

        if (gradeLevel != null) {
            if (gradeLevel < 1 || gradeLevel > 12) {
                throw new IllegalArgumentException("Grade level must be between 1 and 12, got: " + gradeLevel);
            }
            student.setGradeLevel(gradeLevel);
            logger.info("Updated student {}: gradeLevel to {}", id, gradeLevel);
        }

        student.setUpdatedAt(Instant.now());
        return studentRepository.save(student);
    }

    /**
     * Update enrollment status
     */
    public Student updateEnrollmentStatus(UUID id, EnrollmentStatus status) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        student.updateEnrollmentStatus(status);
        logger.info("Updated enrollment status for student {} to {}", id, status);

        return studentRepository.save(student);
    }

    /**
     * Promote student to next grade
     */
    public Student promoteStudent(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));

        student.promoteToNextGrade();
        logger.info("Promoted student {} to grade {}", id, student.getGradeLevel());

        return studentRepository.save(student);
    }
}
