package com.visor.school.academic.model;

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
 * Student entity for K12 school management (grades 1-12)
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_students_student_id", columnList = "student_id", unique = true),
    @Index(name = "idx_students_user_id", columnList = "user_id"),
    @Index(name = "idx_students_grade_level", columnList = "grade_level"),
    @Index(name = "idx_students_enrollment_status", columnList = "enrollment_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "grade_level", nullable = false)
    private int gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false)
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.ENROLLED;

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "emergency_contact_name")),
        @AttributeOverride(name = "relationship", column = @Column(name = "emergency_contact_relationship")),
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "emergency_contact_phone")),
        @AttributeOverride(name = "email", column = @Column(name = "emergency_contact_email")),
        @AttributeOverride(name = "address", column = @Column(name = "emergency_contact_address"))
    })
    private EmergencyContact emergencyContact;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0L;

    @Column(name = "graduated_at")
    private Instant graduatedAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private List<StudentClassLeadership> leadershipPositions = new ArrayList<>();

    public Student(UUID userId, String studentId, String firstName, String lastName, LocalDate dateOfBirth, int gradeLevel) {
        validateGradeLevel(gradeLevel);
        this.userId = userId;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gradeLevel = gradeLevel;
        this.enrollmentStatus = EnrollmentStatus.ENROLLED;
        this.enrolledAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Student(String studentId, UUID userId, String firstName, String lastName, LocalDate dateOfBirth, 
                   int gradeLevel, EnrollmentStatus enrollmentStatus, Address address, EmergencyContact emergencyContact) {
        validateGradeLevel(gradeLevel);
        this.studentId = studentId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gradeLevel = gradeLevel;
        this.enrollmentStatus = enrollmentStatus;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.enrolledAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateGradeLevel(int gradeLevel) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12 (K12 system), got: " + gradeLevel);
        }
    }

    public void promoteToNextGrade() {
        if (gradeLevel >= 12) {
            throw new IllegalArgumentException("Cannot promote beyond grade 12");
        }
        gradeLevel++;
        updatedAt = Instant.now();
    }

    public void updateEnrollmentStatus(EnrollmentStatus status) {
        this.enrollmentStatus = status;
        this.updatedAt = Instant.now();
        if (status == EnrollmentStatus.GRADUATED) {
            this.graduatedAt = Instant.now();
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
