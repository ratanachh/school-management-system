package com.visor.school.academicservice.controller;

import com.visor.school.academicservice.model.AcademicRecord;
import com.visor.school.academicservice.model.CourseCompletion;
import com.visor.school.academicservice.model.EnrollmentEntry;
import com.visor.school.academicservice.service.AcademicRecordService;
import com.visor.school.common.api.ApiResponse;
import static com.visor.school.academicservice.util.ApiResponseHelper.success;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Academic record controller
 * Requires TEACHER role or ADMINISTRATOR role for viewing records
 */
@RestController
@RequestMapping("/api/v1/academic-records")
public class AcademicRecordController {

    private final AcademicRecordService academicRecordService;

    public AcademicRecordController(AcademicRecordService academicRecordService) {
        this.academicRecordService = academicRecordService;
    }

    /**
     * Get academic record for a student
     */
    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('PARENT')")
    public ResponseEntity<ApiResponse<AcademicRecordResponse>> getAcademicRecord(@PathVariable UUID studentId) {
        AcademicRecord record = academicRecordService.getAcademicRecord(studentId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(AcademicRecordResponse.from(record)));
    }

    /**
     * Generate and download transcript PDF
     */
    @GetMapping("/{studentId}/transcript")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('PARENT')")
    public ResponseEntity<byte[]> getTranscript(@PathVariable UUID studentId) {
        byte[] transcript = academicRecordService.generateTranscript(studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("transcript_" + studentId + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(transcript);
    }
}

record AcademicRecordResponse(
        UUID id,
        UUID studentId,
        List<EnrollmentEntryResponse> enrollmentHistory,
        List<CourseCompletionResponse> completedCourses,
        String currentGPA,
        String cumulativeGPA,
        int creditsEarned,
        int creditsRequired,
        String academicStanding,
        String graduationDate
) {
    public static AcademicRecordResponse from(AcademicRecord record) {
        List<EnrollmentEntryResponse> enrollmentHistory = record.getEnrollmentHistory().stream()
                .map(EnrollmentEntryResponse::from)
                .collect(Collectors.toList());

        List<CourseCompletionResponse> completedCourses = record.getCompletedCourses().stream()
                .map(CourseCompletionResponse::from)
                .collect(Collectors.toList());

        return new AcademicRecordResponse(
                record.getId(),
                record.getStudentId(),
                enrollmentHistory,
                completedCourses,
                record.getCurrentGPA().toString(),
                record.getCumulativeGPA().toString(),
                record.getCreditsEarned(),
                record.getCreditsRequired(),
                record.getAcademicStanding().name(),
                record.getGraduationDate() != null ? record.getGraduationDate().toString() : null
        );
    }
}

record EnrollmentEntryResponse(
        String academicYear,
        String term,
        int gradeLevel,
        String enrollmentDate,
        String status
) {
    public static EnrollmentEntryResponse from(EnrollmentEntry entry) {
        return new EnrollmentEntryResponse(
                entry.getAcademicYear(),
                entry.getTerm().name(),
                entry.getGradeLevel(),
                entry.getEnrollmentDate().toString(),
                entry.getStatus().name()
        );
    }
}

record CourseCompletionResponse(
        String courseName,
        String subject,
        int gradeLevel,
        String finalGrade,
        int credits,
        String completionDate
) {
    public static CourseCompletionResponse from(CourseCompletion completion) {
        return new CourseCompletionResponse(
                completion.getCourseName(),
                completion.getSubject(),
                completion.getGradeLevel(),
                completion.getFinalGrade(),
                completion.getCredits(),
                completion.getCompletionDate().toString()
        );
    }
}
