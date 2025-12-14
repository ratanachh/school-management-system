package com.visor.school.academicservice.controller

import com.visor.school.academicservice.model.AcademicRecord
import com.visor.school.academicservice.service.AcademicRecordService
import com.visor.school.common.api.ApiResponse
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Academic record controller
 * Requires TEACHER role or ADMINISTRATOR role for viewing records
 */
@RestController
@RequestMapping("/api/v1/academic-records")
class AcademicRecordController(
    private val academicRecordService: AcademicRecordService
) {

    /**
     * Get academic record for a student
     */
    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('PARENT')")
    fun getAcademicRecord(@PathVariable studentId: UUID): ResponseEntity<ApiResponse<AcademicRecordResponse>> {
        val record = academicRecordService.getAcademicRecord(studentId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(AcademicRecordResponse.from(record)))
    }

    /**
     * Generate and download transcript PDF
     */
    @GetMapping("/{studentId}/transcript")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER') or hasRole('PARENT')")
    fun getTranscript(@PathVariable studentId: UUID): ResponseEntity<ByteArray> {
        val transcript = academicRecordService.generateTranscript(studentId)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.contentDisposition = ContentDisposition.builder("attachment").filename("transcript_$studentId.pdf").build()

        return ResponseEntity.ok()
            .headers(headers)
            .body(transcript)
    }
}

data class AcademicRecordResponse(
    val id: UUID,
    val studentId: UUID,
    val enrollmentHistory: List<EnrollmentEntryResponse>,
    val completedCourses: List<CourseCompletionResponse>,
    val currentGPA: String,
    val cumulativeGPA: String,
    val creditsEarned: Int,
    val creditsRequired: Int,
    val academicStanding: String,
    val graduationDate: String?
) {
    companion object {
        fun from(record: AcademicRecord): AcademicRecordResponse {
            return AcademicRecordResponse(
                id = record.id,
                studentId = record.studentId,
                enrollmentHistory = record.enrollmentHistory.map { EnrollmentEntryResponse.from(it) },
                completedCourses = record.completedCourses.map { CourseCompletionResponse.from(it) },
                currentGPA = record.currentGPA.toString(),
                cumulativeGPA = record.cumulativeGPA.toString(),
                creditsEarned = record.creditsEarned,
                creditsRequired = record.creditsRequired,
                academicStanding = record.academicStanding.name,
                graduationDate = record.graduationDate?.toString()
            )
        }
    }
}

data class EnrollmentEntryResponse(
    val academicYear: String,
    val term: String,
    val gradeLevel: Int,
    val enrollmentDate: String,
    val status: String
) {
    companion object {
        fun from(entry: com.visor.school.academicservice.model.EnrollmentEntry): EnrollmentEntryResponse {
            return EnrollmentEntryResponse(
                academicYear = entry.academicYear,
                term = entry.term.name,
                gradeLevel = entry.gradeLevel,
                enrollmentDate = entry.enrollmentDate.toString(),
                status = entry.status.name
            )
        }
    }
}

data class CourseCompletionResponse(
    val courseName: String,
    val subject: String,
    val gradeLevel: Int,
    val finalGrade: String,
    val credits: Int,
    val completionDate: String
) {
    companion object {
        fun from(completion: com.visor.school.academicservice.model.CourseCompletion): CourseCompletionResponse {
            return CourseCompletionResponse(
                courseName = completion.courseName,
                subject = completion.subject,
                gradeLevel = completion.gradeLevel,
                finalGrade = completion.finalGrade,
                credits = completion.credits,
                completionDate = completion.completionDate.toString()
            )
        }
    }
}
