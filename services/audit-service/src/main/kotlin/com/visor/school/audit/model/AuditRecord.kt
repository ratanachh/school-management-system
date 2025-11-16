package com.visor.school.audit.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Audit record entity for logging security-relevant events
 */
@Entity
@Table(name = "audit_records", indexes = [
    Index(name = "idx_audit_user", columnList = "user_id"),
    Index(name = "idx_audit_action", columnList = "action"),
    Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    Index(name = "idx_audit_resource", columnList = "resource_type,resource_id"),
    Index(name = "idx_audit_user_timestamp", columnList = "user_id,timestamp")
])
class AuditRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    val action: AuditAction,

    @Column(name = "resource_type", nullable = false)
    val resourceType: String,

    @Column(name = "resource_id")
    val resourceId: String? = null,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,

    @Column(name = "details", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter::class)
    val details: Map<String, Any>? = null,

    @Column(name = "success", nullable = false)
    val success: Boolean = true,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(name = "timestamp", nullable = false)
    val timestamp: Instant = Instant.now()
) {
    init {
        require(resourceType.isNotBlank()) {
            "Resource type cannot be blank"
        }
    }
}

/**
 * Converter for JSON map storage
 */
@Converter
class JsonMapConverter : jakarta.persistence.AttributeConverter<Map<String, Any>, String> {
    override fun convertToDatabaseColumn(attribute: Map<String, Any>?): String? {
        if (attribute == null) return null
        return com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any>? {
        if (dbData == null) return null
        return com.fasterxml.jackson.databind.ObjectMapper().readValue(dbData, Map::class.java) as? Map<String, Any>
    }
}

