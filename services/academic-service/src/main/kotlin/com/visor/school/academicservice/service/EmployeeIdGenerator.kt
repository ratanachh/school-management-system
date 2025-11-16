package com.visor.school.academicservice.service

import org.springframework.stereotype.Service
import java.time.Year

/**
 * Service for generating unique employee IDs
 * Format: EMP-YYYY-XXXXX (e.g., EMP-2025-00001)
 */
@Service
class EmployeeIdGenerator {
    private var sequenceCounter = 0

    /**
     * Generate a unique employee ID
     * Format: EMP-{YEAR}-{5-digit-sequence}
     */
    fun generateEmployeeId(): String {
        val year = Year.now().value
        val sequence = String.format("%05d", ++sequenceCounter)
        return "EMP-$year-$sequence"
    }

    /**
     * Generate employee ID with custom sequence
     */
    fun generateEmployeeId(sequence: Int): String {
        val year = Year.now().value
        val formattedSequence = String.format("%05d", sequence)
        return "EMP-$year-$formattedSequence"
    }
}

