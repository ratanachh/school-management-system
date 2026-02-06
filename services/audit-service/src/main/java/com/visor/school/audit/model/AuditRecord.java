package com.visor.school.audit.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Audit record entity for logging security-relevant events
 */
@Entity
@Table(name = "audit_records", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_resource", columnList = "resource_type,resource_id"),
    @Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp")
})
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "details", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> details;

    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    public AuditRecord() {
    }

    public AuditRecord(UUID userId, AuditAction action, String resourceType, String resourceId, String ipAddress, String userAgent, Map<String, Object> details, boolean success, String errorMessage) {
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Resource type cannot be blank");
        }
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }
    
    // For testing/manual creation with specific ID/Timestamp if needed, but the kotlin one had defaults.
    // The Kotlin constructor had default values.
    // id = UUID.randomUUID()
    // success = true
    // timestamp = Instant.now()
    
    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Resource type cannot be blank");
        }
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Converter for JSON map storage
     */
    @Converter
    public static class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {
        private static final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Map<String, Object> attribute) {
            if (attribute == null) return null;
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting map to JSON", e);
            }
        }

        @Override
        public Map<String, Object> convertToEntityAttribute(String dbData) {
            if (dbData == null) return null;
            try {
                return objectMapper.readValue(dbData, Map.class);
            } catch (IOException e) {
                throw new RuntimeException("Error converting JSON to map", e);
            }
        }
    }
}
