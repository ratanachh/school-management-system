package com.visor.school.assessment.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Converter for JSON map storage in PostgreSQL JSONB columns
 */
@Converter
class JsonMapConverter : AttributeConverter<Map<String, Any>, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, Any>?): String? {
        if (attribute == null) return null
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, Any>? {
        if (dbData == null) return null
        return objectMapper.readValue(dbData, Map::class.java) as? Map<String, Any>
    }
}

