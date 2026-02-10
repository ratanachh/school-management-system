package com.visor.school.userservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permissions_key", columnList = "permission_key", unique = true)
})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "permission_key", nullable = false, unique = true)
    private String permissionKey;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Permission() {}

    public Permission(String permissionKey, String name, String description, String category) {
        this.permissionKey = permissionKey;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }
}
