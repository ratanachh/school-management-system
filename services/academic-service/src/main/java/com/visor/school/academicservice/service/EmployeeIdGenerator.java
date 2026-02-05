package com.visor.school.academicservice.service;

import org.springframework.stereotype.Service;

import java.time.Year;

/**
 * Service for generating unique employee IDs
 * Format: EMP-YYYY-XXXXX (e.g., EMP-2025-00001)
 */
@Service
public class EmployeeIdGenerator {
    private int sequenceCounter = 0;

    /**
     * Generate a unique employee ID
     * Format: EMP-{YEAR}-{5-digit-sequence}
     */
    public String generateEmployeeId() {
        int year = Year.now().getValue();
        String sequence = String.format("%05d", ++sequenceCounter);
        return "EMP-" + year + "-" + sequence;
    }

    /**
     * Generate employee ID with custom sequence
     */
    public String generateEmployeeId(int sequence) {
        int year = Year.now().getValue();
        String formattedSequence = String.format("%05d", sequence);
        return "EMP-" + year + "-" + formattedSequence;
    }
}
