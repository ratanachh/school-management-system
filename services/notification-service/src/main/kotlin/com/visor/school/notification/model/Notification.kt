package com.visor.school.notification.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * Notification entity for important events
 */
@Entity
@Table(name = "notifications", indexes = [
    Index(name = "idx_notification_user", columnList = "user_id"),
    Index(name = "idx_notification_read", columnList = "user_id,read"),
    Index(name = "idx_notification_created", columnList = "created_at"),
    Index(name = "idx_notification_type", columnList = "type")
])
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: NotificationType,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "metadata", columnDefinition = "JSONB")
    @Convert(converter = JsonMapConverter::class)
    val metadata: Map<String, Any>? = null,

    @Column(name = "read", nullable = false)
    var read: Boolean = false,

    @Column(name = "read_at")
    var readAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    init {
        require(title.isNotBlank()) {
            "Title cannot be blank"
        }
        require(message.isNotBlank()) {
            "Message cannot be blank"
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

