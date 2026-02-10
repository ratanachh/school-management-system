package com.visor.school.userservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Parent entity linking users to their children (students)
 */
@Entity
@Table(name = "parents", indexes = {
    @Index(name = "idx_parents_user_id", columnList = "user_id"),
    @Index(name = "idx_parents_student_id", columnList = "student_id"),
    @Index(name = "idx_parents_unique", columnList = "user_id,student_id", unique = true)
})
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false)
    private Relationship relationship;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Parent() {}

    public Parent(UUID userId, UUID studentId, Relationship relationship, boolean isPrimary) {
        this.userId = userId;
        this.studentId = studentId;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
        this.updatedAt = Instant.now();
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
        this.updatedAt = Instant.now();
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
