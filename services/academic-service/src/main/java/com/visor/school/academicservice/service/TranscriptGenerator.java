package com.visor.school.academicservice.service;

import com.visor.school.academicservice.model.AcademicRecord;
import com.visor.school.academicservice.model.CourseCompletion;
import com.visor.school.academicservice.model.EnrollmentEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Transcript PDF generation service
 * Generates official academic transcripts in PDF format
 */
@Service
public class TranscriptGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TranscriptGenerator.class);

    /**
     * Generate PDF transcript for a student's academic record
     */
    public byte[] generate(AcademicRecord academicRecord) {
        logger.info("Generating transcript for student: {}", academicRecord.getStudentId());

        // For now, return a simple text-based representation
        // In production, this would use a PDF library like iText or Apache PDFBox
        StringBuilder transcript = new StringBuilder();
        transcript.append("OFFICIAL ACADEMIC TRANSCRIPT\n");
        transcript.append("=".repeat(50)).append("\n");
        transcript.append("\n");
        transcript.append("Student ID: ").append(academicRecord.getStudentId()).append("\n");
        transcript.append("Current GPA: ").append(academicRecord.getCurrentGPA()).append("\n");
        transcript.append("Cumulative GPA: ").append(academicRecord.getCumulativeGPA()).append("\n");
        transcript.append("Credits Earned: ").append(academicRecord.getCreditsEarned())
                .append(" / ").append(academicRecord.getCreditsRequired()).append("\n");
        transcript.append("Academic Standing: ").append(academicRecord.getAcademicStanding()).append("\n");
        transcript.append("\n");

        if (!academicRecord.getEnrollmentHistory().isEmpty()) {
            transcript.append("ENROLLMENT HISTORY\n");
            transcript.append("-".repeat(50)).append("\n");
            for (EnrollmentEntry entry : academicRecord.getEnrollmentHistory()) {
                transcript.append(entry.getAcademicYear()).append(" - ")
                        .append(entry.getTerm().name()).append(" - Grade ")
                        .append(entry.getGradeLevel()).append(" - ")
                        .append(entry.getStatus().name()).append("\n");
            }
            transcript.append("\n");
        }

        if (!academicRecord.getCompletedCourses().isEmpty()) {
            transcript.append("COMPLETED COURSES\n");
            transcript.append("-".repeat(50)).append("\n");
            for (CourseCompletion course : academicRecord.getCompletedCourses()) {
                transcript.append(course.getCourseName()).append(" (")
                        .append(course.getSubject()).append(") - Grade ")
                        .append(course.getGradeLevel()).append(" - Grade: ")
                        .append(course.getFinalGrade()).append(" - Credits: ")
                        .append(course.getCredits()).append("\n");
            }
            transcript.append("\n");
        }

        if (academicRecord.getGraduationDate() != null) {
            transcript.append("Graduation Date: ").append(academicRecord.getGraduationDate()).append("\n");
        }

        // Convert to byte array (in production, this would be actual PDF generation)
        return transcript.toString().getBytes();
    }
}
