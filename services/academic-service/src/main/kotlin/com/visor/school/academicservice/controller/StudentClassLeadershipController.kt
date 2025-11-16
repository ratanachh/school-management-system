package com.visor.school.academicservice.controller

import com.visor.school.academicservice.model.LeadershipPosition
import com.visor.school.academicservice.model.StudentClassLeadership
import com.visor.school.academicservice.service.StudentClassLeadershipService
import com.visor.school.common.api.ApiResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Student class leadership controller
 * Requires ADMINISTRATOR role or class teacher permissions
 */
@RestController
@RequestMapping("/api/v1/classes/{classId}/leaders")
class StudentClassLeadershipController(
    private val leadershipService: StudentClassLeadershipService
) {

    /**
     * Assign class leader to a class
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun assignClassLeader(
        @PathVariable classId: UUID,
        @Valid @RequestBody request: AssignLeaderRequest
    ): ResponseEntity<ApiResponse<StudentClassLeadershipResponse>> {
        val leadership = leadershipService.assignLeader(
            studentId = request.studentId,
            classId = classId,
            position = request.position,
            assignedBy = request.assignedBy // From JWT token in production
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(StudentClassLeadershipResponse.from(leadership), "Class leader assigned successfully"))
    }

    /**
     * Get all class leaders for a class
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getClassLeaders(@PathVariable classId: UUID): ResponseEntity<ApiResponse<List<StudentClassLeadershipResponse>>> {
        val leaders = leadershipService.getLeadersByClass(classId)
        return ResponseEntity.ok(ApiResponse.success(leaders.map { StudentClassLeadershipResponse.from(it) }))
    }

    /**
     * Get class leader by position
     */
    @GetMapping("/position/{position}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun getLeaderByPosition(
        @PathVariable classId: UUID,
        @PathVariable position: LeadershipPosition
    ): ResponseEntity<ApiResponse<StudentClassLeadershipResponse>> {
        val leader = leadershipService.getLeaderByPosition(classId, position)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(StudentClassLeadershipResponse.from(leader)))
    }

    /**
     * Check if student is a class leader for the class
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    fun checkClassLeader(
        @PathVariable classId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val isLeader = leadershipService.isClassLeader(studentId, classId)
        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf(
                    "isClassLeader" to isLeader,
                    "studentId" to studentId.toString(),
                    "classId" to classId.toString()
                )
            )
        )
    }

    /**
     * Remove class leader assignment
     */
    @DeleteMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun removeClassLeader(
        @PathVariable classId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        leadershipService.removeLeader(studentId, classId)
        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("message" to "Class leader assignment removed successfully")
            )
        )
    }
}

data class AssignLeaderRequest(
    @field:NotNull
    val studentId: UUID,

    @field:NotNull
    val position: LeadershipPosition,

    @field:NotNull
    val assignedBy: UUID // From JWT token in production
)

data class StudentClassLeadershipResponse(
    val id: UUID,
    val studentId: UUID,
    val classId: UUID,
    val leadershipPosition: String,
    val assignedBy: UUID,
    val assignedAt: java.time.Instant
) {
    companion object {
        fun from(leadership: StudentClassLeadership): StudentClassLeadershipResponse {
            return StudentClassLeadershipResponse(
                id = leadership.id,
                studentId = leadership.studentId,
                classId = leadership.classId,
                leadershipPosition = leadership.leadershipPosition.name,
                assignedBy = leadership.assignedBy,
                assignedAt = leadership.assignedAt
            )
        }
    }
}

