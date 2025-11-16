package com.visor.school.academicservice.service

import org.springframework.stereotype.Service
import java.time.Year

/**
 * Service for generating unique student IDs
 * Format: YYYY-XXXXX (e.g., 2025-00001)
 */
@Service
class StudentIdGenerator {
    private var sequenceCounter = 0

    /**
     * Generate a unique student ID
     * Format: {YEAR}-{5-digit-sequence}
     */
    fun generateStudentId(): String {
        val year = Year.now().value
        val sequence = String.format("%05d", ++sequenceCounter)
        return "$year-$sequence"
    }

    /**
     * Generate student ID with custom sequence
     */
    fun generateStudentId(sequence: Int): String {
        val year = Year.now().value
        val formattedSequence = String.format("%05d", sequence)
        return "$year-$formattedSequence"
    }
}

