package com.visor.school.academicservice.service;

import org.springframework.stereotype.Service;

import java.time.Year;

/**
 * Service for generating unique student IDs
 * Format: YYYY-XXXXX (e.g., 2025-00001)
 */
@Service
public class StudentIdGenerator {
    private int sequenceCounter = 0;

    /**
     * Generate a unique student ID
     * Format: {YEAR}-{5-digit-sequence}
     */
    public String generateStudentId() {
        int year = Year.now().getValue();
        String sequence = String.format("%05d", ++sequenceCounter);
        return year + "-" + sequence;
    }

    /**
     * Generate student ID with custom sequence
     */
    public String generateStudentId(int sequence) {
        int year = Year.now().getValue();
        String formattedSequence = String.format("%05d", sequence);
        return year + "-" + formattedSequence;
    }
}
