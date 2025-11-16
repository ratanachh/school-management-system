package com.visor.school.academicservice.model

import jakarta.persistence.Embeddable
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Schedule value object for class scheduling
 */
@Embeddable
data class Schedule(
    val daysOfWeek: String, // Comma-separated days (e.g., "MONDAY,WEDNESDAY,FRIDAY")
    val startTime: LocalTime,
    val endTime: LocalTime,
    val room: String? = null
) {
    fun getDays(): List<DayOfWeek> {
        return daysOfWeek.split(",").mapNotNull { day ->
            try {
                DayOfWeek.valueOf(day.trim().uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun getDurationMinutes(): Int {
        return java.time.Duration.between(startTime, endTime).toMinutes().toInt()
    }
}

