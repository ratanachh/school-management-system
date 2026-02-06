package com.visor.school.academic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Teacher assignment to a class
 * For grades 7-12, one teacher can be designated as class teacher/coordinator (isClassTeacher = true)
 */
@Entity
@Table(name = "teacher_assignments", indexes = {
    @Index(name = "idx_teacher_assignments_teacher", columnList = "teacher_id"),
    @Index(name = "idx_teacher_assignments_class", columnList = "class_id"),
    @Index(name = "idx_teacher_assignments_unique", columnList = "teacher_id,class_id", unique = true),
    @Index(name = "idx_teacher_assignments_class_teacher", columnList = "class_id,is_class_teacher")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "is_class_teacher", nullable = false)
    private boolean isClassTeacher = false;

    @Column(name = "assigned_date", nullable = false)
    private Instant assignedDate = Instant.now();

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TeacherAssignment(UUID teacherId, UUID classId, boolean isClassTeacher) {
        this.teacherId = teacherId;
        this.classId = classId;
        this.isClassTeacher = isClassTeacher;
        this.assignedDate = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
