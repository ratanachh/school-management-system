package com.visor.school.userservice.controller

import com.visor.school.common.api.ApiResponse
import com.visor.school.userservice.model.Relationship
import com.visor.school.userservice.service.ParentService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Parent controller for managing parent-student relationships
 */
@RestController
@RequestMapping("/v1/parents")
class ParentController(
    private val parentService: ParentService
) {

    /**
     * Link a student to a parent
     */
    @PostMapping("/{userId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun linkStudentToParent(
        @PathVariable userId: UUID,
        @PathVariable studentId: UUID,
        @Valid @RequestBody request: LinkStudentRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        parentService.linkStudentToParent(
            userId = userId,
            studentId = studentId,
            relationship = request.relationship,
            isPrimary = request.isPrimary ?: false
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    mapOf(
                        "message" to "Student linked to parent successfully",
                        "userId" to userId.toString(),
                        "studentId" to studentId.toString()
                    )
                )
            )
    }

    /**
     * Get all children (students) for a parent
     */
    @GetMapping("/{parentId}/students")
    @PreAuthorize("hasRole('ADMINISTRATOR') or @securityContextService.isCurrentUserId(#parentId)")
    fun getChildren(@PathVariable parentId: UUID): ResponseEntity<ApiResponse<List<ParentStudentResponse>>> {
        val parents = parentService.getChildrenByParent(parentId)
        
        val responses = parents.map {
            ParentStudentResponse(
                parentId = it.userId,
                studentId = it.studentId,
                relationship = it.relationship.name,
                isPrimary = it.isPrimary
            )
        }

        return ResponseEntity.ok(ApiResponse.success(responses))
    }

    /**
     * Unlink student from parent
     */
    @DeleteMapping("/{userId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    fun unlinkStudentFromParent(
        @PathVariable userId: UUID,
        @PathVariable studentId: UUID
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        parentService.unlinkStudentFromParent(userId, studentId)

        return ResponseEntity.ok(
            ApiResponse.success(
                mapOf("message" to "Student unlinked from parent successfully")
            )
        )
    }
}

data class LinkStudentRequest(
    @field:NotNull
    val relationship: Relationship,
    val isPrimary: Boolean? = false
)

data class ParentStudentResponse(
    val parentId: UUID,
    val studentId: UUID,
    val relationship: String,
    val isPrimary: Boolean
)

