package com.visor.school.userservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_email_verification_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_email_verification_tokens_expires_at", columnList = "expires_at")
})
public class EmailVerificationToken {

    @Id
    @Column(length = 64, nullable = false)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EmailVerificationToken() {}

    public EmailVerificationToken(String token, UUID userId, String email, Instant expiresAt, Instant createdAt) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
