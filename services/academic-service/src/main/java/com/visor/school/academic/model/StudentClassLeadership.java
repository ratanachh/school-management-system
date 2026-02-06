package com.visor.school.academic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Student class leadership assignment
 * Links students to their leadership positions in classes
 */
@Entity
@Table(name = "student_class_leadership", indexes = {
    @Index(name = "idx_leadership_student", columnList = "student_id"),
    @Index(name = "idx_leadership_class", columnList = "class_id"),
    @Index(name = "idx_leadership_class_position", columnList = "class_id,leadership_position", unique = true),
    @Index(name = "idx_leadership_student_class", columnList = "student_id,class_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentClassLeadership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Enumerated(EnumType.STRING)
    @Column(name = "leadership_position", nullable = false)
    private LeadershipPosition leadershipPosition;

    @Column(name = "assigned_by", nullable = false)
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StudentClassLeadership(UUID studentId, UUID classId, LeadershipPosition leadershipPosition, UUID assignedBy) {
        this.studentId = studentId;
        this.classId = classId;
        this.leadershipPosition = leadershipPosition;
        this.assignedBy = assignedBy;
        this.assignedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
