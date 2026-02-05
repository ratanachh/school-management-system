package com.visor.school.academicservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Class entity supporting both homeroom classes (grades 1-6) and subject classes (all grades)
 */
@Entity
@Table(name = "classes", indexes = {
    @Index(name = "idx_classes_name", columnList = "class_name"),
    @Index(name = "idx_classes_grade_level", columnList = "grade_level"),
    @Index(name = "idx_classes_class_type", columnList = "class_type"),
    @Index(name = "idx_classes_homeroom_teacher", columnList = "homeroom_teacher_id"),
    @Index(name = "idx_classes_class_teacher", columnList = "class_teacher_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_type", nullable = false)
    private ClassType classType;

    @Column(name = "subject")
    private String subject;

    @Column(name = "grade_level", nullable = false)
    private int gradeLevel;

    @Column(name = "homeroom_teacher_id")
    private UUID homeroomTeacherId;

    @Column(name = "class_teacher_id")
    private UUID classTeacherId;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "term", nullable = false)
    private Term term;

    @Embedded
    private Schedule schedule;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "current_enrollment", nullable = false)
    private int currentEnrollment = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClassStatus status = ClassStatus.SCHEDULED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0L;

    public Class(String className, ClassType classType, String subject, int gradeLevel, 
                 UUID homeroomTeacherId, UUID classTeacherId, String academicYear, Term term,
                 Schedule schedule, Integer maxCapacity, LocalDate startDate, LocalDate endDate,
                 ClassStatus status) {
        validateGradeLevel(gradeLevel);
        validateClassType(classType, gradeLevel, subject, homeroomTeacherId, classTeacherId);
        
        this.className = className;
        this.classType = classType;
        this.subject = subject;
        this.gradeLevel = gradeLevel;
        this.homeroomTeacherId = homeroomTeacherId;
        this.classTeacherId = classTeacherId;
        this.academicYear = academicYear;
        this.term = term;
        this.schedule = schedule;
        this.maxCapacity = maxCapacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateGradeLevel(int gradeLevel) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12 (K12 system), got: " + gradeLevel);
        }
    }

    private void validateClassType(ClassType classType, int gradeLevel, String subject, 
                                   UUID homeroomTeacherId, UUID classTeacherId) {
        switch (classType) {
            case HOMEROOM -> {
                if (gradeLevel < 1 || gradeLevel > 6) {
                    throw new IllegalArgumentException("Homeroom classes are only for grades 1-6, got: " + gradeLevel);
                }
                if (subject != null) {
                    throw new IllegalArgumentException("Homeroom classes should not have a subject");
                }
                if (classTeacherId != null) {
                    throw new IllegalArgumentException("Homeroom classes should not have a class teacher (use homeroomTeacherId)");
                }
            }
            case SUBJECT -> {
                if (subject == null) {
                    throw new IllegalArgumentException("Subject classes must have a subject");
                }
                if (homeroomTeacherId != null) {
                    throw new IllegalArgumentException("Subject classes should not have a homeroom teacher");
                }
            }
        }
    }

    public void updateStatus(ClassStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public void incrementEnrollment() {
        this.currentEnrollment++;
        this.updatedAt = Instant.now();
    }

    public void decrementEnrollment() {
        if (currentEnrollment <= 0) {
            throw new IllegalArgumentException("Cannot decrement enrollment below 0");
        }
        this.currentEnrollment--;
        this.updatedAt = Instant.now();
    }
}
