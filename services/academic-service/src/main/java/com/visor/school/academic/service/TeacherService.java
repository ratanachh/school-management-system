package com.visor.school.academic.service;

import com.visor.school.academic.model.EmploymentStatus;
import com.visor.school.academic.model.Teacher;
import com.visor.school.academic.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Teacher service
 */
@Service
@Transactional
public class TeacherService {
    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    private final TeacherRepository teacherRepository;
    private final EmployeeIdGenerator employeeIdGenerator;

    public TeacherService(
            TeacherRepository teacherRepository,
            EmployeeIdGenerator employeeIdGenerator
    ) {
        this.teacherRepository = teacherRepository;
        this.employeeIdGenerator = employeeIdGenerator;
    }

    /**
     * Create a new teacher
     */
    public Teacher createTeacher(
            UUID userId,
            List<String> qualifications,
            List<String> subjectSpecializations,
            LocalDate hireDate,
            String department
    ) {
        logger.info("Creating teacher for user: {}", userId);

        if (subjectSpecializations.isEmpty()) {
            throw new IllegalArgumentException("Teacher must have at least one subject specialization");
        }

        // Check if teacher with this user ID already exists
        if (teacherRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Teacher with user ID " + userId + " already exists");
        }

        String employeeId = employeeIdGenerator.generateEmployeeId();

        Teacher teacher = new Teacher(
                employeeId,
                userId,
                qualifications,
                subjectSpecializations,
                hireDate,
                department,
                EmploymentStatus.ACTIVE
        );

        Teacher saved = teacherRepository.save(teacher);
        logger.info("Teacher created successfully: {}", saved.getEmployeeId());

        return saved;
    }

    /**
     * Get teacher by ID
     */
    @Transactional(readOnly = true)
    public Teacher getTeacherById(UUID id) {
        return teacherRepository.findById(id).orElse(null);
    }

    /**
     * Get teacher by user ID
     */
    @Transactional(readOnly = true)
    public Teacher getTeacherByUserId(UUID userId) {
        return teacherRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Get teacher by employee ID
     */
    @Transactional(readOnly = true)
    public Teacher getTeacherByEmployeeId(String employeeId) {
        return teacherRepository.findByEmployeeId(employeeId).orElse(null);
    }

    /**
     * Get teachers by employment status
     */
    @Transactional(readOnly = true)
    public List<Teacher> getTeachersByStatus(EmploymentStatus status) {
        return teacherRepository.findByEmploymentStatus(status);
    }

    /**
     * Get teachers by department
     */
    @Transactional(readOnly = true)
    public List<Teacher> getTeachersByDepartment(String department) {
        return teacherRepository.findByDepartment(department);
    }

    /**
     * Update employment status
     */
    public Teacher updateEmploymentStatus(UUID id, EmploymentStatus status) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));

        teacher.updateEmploymentStatus(status);
        logger.info("Updated employment status for teacher {} to {}", id, status);

        return teacherRepository.save(teacher);
    }
}
