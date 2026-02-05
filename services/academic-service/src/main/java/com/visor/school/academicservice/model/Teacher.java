package com.visor.school.academicservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Teacher entity
 */
@Entity
@Table(name = "teachers", indexes = {
    @Index(name = "idx_teachers_employee_id", columnList = "employee_id", unique = true),
    @Index(name = "idx_teachers_user_id", columnList = "user_id"),
    @Index(name = "idx_teachers_employment_status", columnList = "employment_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ElementCollection
    @CollectionTable(name = "teacher_qualifications", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "qualification")
    private List<String> qualifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "teacher_subject_specializations", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject")
    private List<String> subjectSpecializations = new ArrayList<>();

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "department")
    private String department;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0L;

    public Teacher(UUID userId, String employeeId, List<String> qualifications, List<String> subjectSpecializations, LocalDate hireDate, String department) {
        if (subjectSpecializations == null || subjectSpecializations.isEmpty()) {
            throw new IllegalArgumentException("Teacher must have at least one subject specialization");
        }
        this.userId = userId;
        this.employeeId = employeeId;
        this.qualifications = qualifications != null ? qualifications : new ArrayList<>();
        this.subjectSpecializations = subjectSpecializations;
        this.hireDate = hireDate;
        this.department = department;
        this.employmentStatus = EmploymentStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Teacher(String employeeId, UUID userId, List<String> qualifications, List<String> subjectSpecializations, 
                   LocalDate hireDate, String department, EmploymentStatus employmentStatus) {
        if (subjectSpecializations == null || subjectSpecializations.isEmpty()) {
            throw new IllegalArgumentException("Teacher must have at least one subject specialization");
        }
        this.employeeId = employeeId;
        this.userId = userId;
        this.qualifications = qualifications != null ? qualifications : new ArrayList<>();
        this.subjectSpecializations = subjectSpecializations;
        this.hireDate = hireDate;
        this.department = department;
        this.employmentStatus = employmentStatus;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateEmploymentStatus(EmploymentStatus status) {
        this.employmentStatus = status;
        this.updatedAt = Instant.now();
    }
}
