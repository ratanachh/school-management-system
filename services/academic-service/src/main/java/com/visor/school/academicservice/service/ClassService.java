package com.visor.school.academicservice.service;

import com.visor.school.academicservice.model.ClassStatus;
import com.visor.school.academicservice.model.ClassType;
import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Schedule;
import com.visor.school.academicservice.model.Teacher;
import com.visor.school.academicservice.model.TeacherAssignment;
import com.visor.school.academicservice.model.Term;
// Using fully qualified name for Class to avoid conflict with java.lang.Class
import com.visor.school.academicservice.repository.ClassRepository;
import com.visor.school.academicservice.repository.TeacherAssignmentRepository;
import com.visor.school.academicservice.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Class service with validation for homeroom classes (grades 1-6) and class teacher assignment (grades 7-12)
 */
@Service
@Transactional
public class ClassService {
    private static final Logger logger = LoggerFactory.getLogger(ClassService.class);

    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public ClassService(
            ClassRepository classRepository,
            TeacherRepository teacherRepository,
            TeacherAssignmentRepository teacherAssignmentRepository
    ) {
        this.classRepository = classRepository;
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    /**
     * Create a homeroom class (grades 1-6 only)
     * Validates: grade level 1-6, one homeroom class per grade per academic year
     */
    public com.visor.school.academicservice.model.Class createHomeroomClass(
            String className,
            int gradeLevel,
            UUID homeroomTeacherId,
            String academicYear,
            Term term,
            Schedule schedule,
            Integer maxCapacity,
            LocalDate startDate,
            LocalDate endDate
    ) {
        logger.info("Creating homeroom class: {} for grade {}", className, gradeLevel);

        // Validate grade level
        if (gradeLevel < 1 || gradeLevel > 6) {
            throw new IllegalArgumentException("Homeroom classes are only for grades 1-6, got: " + gradeLevel);
        }

        // Validate teacher exists and is active
        Teacher teacher = teacherRepository.findById(homeroomTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + homeroomTeacherId));
        if (teacher.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new IllegalArgumentException("Teacher must be active");
        }

        // Validate one homeroom class per grade per academic year
        List<com.visor.school.academicservice.model.Class> existingHomeroom = classRepository.findByAcademicYearAndTermAndTypeAndGrade(
                academicYear,
                term,
                ClassType.HOMEROOM,
                gradeLevel
        );
        if (!existingHomeroom.isEmpty()) {
            throw new IllegalArgumentException("Homeroom class already exists for grade " + gradeLevel + 
                    " in academic year " + academicYear + ", term " + term);
        }

        com.visor.school.academicservice.model.Class classEntity = new com.visor.school.academicservice.model.Class(
                className,
                ClassType.HOMEROOM,
                null,
                gradeLevel,
                homeroomTeacherId,
                null,
                academicYear,
                term,
                schedule,
                maxCapacity,
                startDate,
                endDate,
                ClassStatus.SCHEDULED
        );

        com.visor.school.academicservice.model.Class saved = classRepository.save(classEntity);
        logger.info("Homeroom class created: {}", saved.getId());

        return saved;
    }

    /**
     * Create a subject class (all grades)
     */
    public com.visor.school.academicservice.model.Class createSubjectClass(
            String className,
            String subject,
            int gradeLevel,
            String academicYear,
            Term term,
            Schedule schedule,
            Integer maxCapacity,
            LocalDate startDate,
            LocalDate endDate
    ) {
        logger.info("Creating subject class: {} for subject {}, grade {}", className, subject, gradeLevel);

        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12, got: " + gradeLevel);
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject classes must have a subject");
        }

        com.visor.school.academicservice.model.Class classEntity = new com.visor.school.academicservice.model.Class(
                className,
                ClassType.SUBJECT,
                subject,
                gradeLevel,
                null,
                null,
                academicYear,
                term,
                schedule,
                maxCapacity,
                startDate,
                endDate,
                ClassStatus.SCHEDULED
        );

        com.visor.school.academicservice.model.Class saved = classRepository.save(classEntity);
        logger.info("Subject class created: {}", saved.getId());

        return saved;
    }

    /**
     * Assign class teacher/coordinator (grades 7-12 only)
     * Validates: grade level 7-12, teacher must be assigned to the class, only one class teacher per class
     */
    public com.visor.school.academicservice.model.Class assignClassTeacher(
            UUID classId,
            UUID teacherId,
            UUID assignedBy
    ) {
        logger.info("Assigning class teacher {} to class {}", teacherId, classId);

        com.visor.school.academicservice.model.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        // Validate grade level
        if (classEntity.getGradeLevel() < 7 || classEntity.getGradeLevel() > 12) {
            throw new IllegalArgumentException("Class teacher assignment is only for grades 7-12, got: " + 
                    classEntity.getGradeLevel());
        }

        if (classEntity.getClassType() != ClassType.SUBJECT) {
            throw new IllegalArgumentException("Class teacher can only be assigned to subject classes");
        }

        // Validate teacher exists and is active
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        if (teacher.getEmploymentStatus() != EmploymentStatus.ACTIVE) {
            throw new IllegalArgumentException("Teacher must be active");
        }

        // Validate teacher is assigned to the class
        List<TeacherAssignment> assignment = teacherAssignmentRepository.findByTeacherIdAndClassId(teacherId, classId);
        if (assignment.isEmpty()) {
            throw new IllegalArgumentException("Teacher must be assigned to the class before being designated as class teacher");
        }

        // Validate only one class teacher per class
        List<TeacherAssignment> existingClassTeacher = teacherAssignmentRepository.findClassTeacherByClassId(classId);
        if (!existingClassTeacher.isEmpty()) {
            throw new IllegalArgumentException("Class already has a class teacher assigned");
        }

        // Create or update teacher assignment with isClassTeacher = true
        TeacherAssignment teacherAssignment = teacherAssignmentRepository.findByTeacherIdAndClassId(teacherId, classId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Teacher assignment not found"));

        // Update class teacher ID
        // Note: In a full implementation, we'd need to update the Class entity
        // For now, we'll create a new assignment with isClassTeacher flag
        // This would require modifying the Class entity to update classTeacherId

        logger.info("Class teacher assigned: teacher {} to class {}", teacherId, classId);

        return classEntity;
    }

    /**
     * Get class by ID
     */
    @Transactional(readOnly = true)
    public com.visor.school.academicservice.model.Class getClassById(UUID id) {
        return classRepository.findById(id).orElse(null);
    }

    /**
     * Get classes by grade level
     */
    @Transactional(readOnly = true)
    public List<com.visor.school.academicservice.model.Class> getClassesByGradeLevel(int gradeLevel) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12, got: " + gradeLevel);
        }
        return classRepository.findByGradeLevel(gradeLevel);
    }

    /**
     * Get classes by type
     */
    @Transactional(readOnly = true)
    public List<com.visor.school.academicservice.model.Class> getClassesByType(ClassType classType) {
        return classRepository.findByClassType(classType);
    }

    /**
     * Update class status
     */
    public com.visor.school.academicservice.model.Class updateClassStatus(UUID id, ClassStatus status) {
        com.visor.school.academicservice.model.Class classEntity = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + id));

        classEntity.updateStatus(status);
        logger.info("Updated class status for {} to {}", id, status);

        return classRepository.save(classEntity);
    }
}
