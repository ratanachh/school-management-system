package com.visor.school.userservice.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.userservice.dto.LinkStudentRequest;
import com.visor.school.userservice.dto.ParentStudentResponse;
import com.visor.school.userservice.model.Parent;
import com.visor.school.userservice.service.ParentService;

import jakarta.validation.Valid;

/**
 * Parent controller for managing parent-student relationships
 */
@RestController
@RequestMapping("/v1/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    /**
     * Link a student to a parent
     */
    @PostMapping("/{userId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> linkStudentToParent(
        @PathVariable UUID userId,
        @PathVariable UUID studentId,
        @Valid @RequestBody LinkStudentRequest request
    ) {
        parentService.linkStudentToParent(
            userId,
            studentId,
            request.relationship(),
            request.isPrimary() != null ? request.isPrimary() : false
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    Map.of(
                        "message", "Student linked to parent successfully",
                        "userId", userId.toString(),
                        "studentId", studentId.toString()
                    )
                )
            );
    }

    /**
     * Get all children (students) for a parent
     */
    @GetMapping("/{parentId}/students")
    @PreAuthorize("hasRole('ADMINISTRATOR') or @securityContextService.isCurrentUserId(#parentId)")
    public ResponseEntity<ApiResponse<List<ParentStudentResponse>>> getChildren(@PathVariable UUID parentId) {
        List<Parent> parents = parentService.getChildrenByParent(parentId);

        List<ParentStudentResponse> responses = parents.stream()
            .map(ParentStudentResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Unlink student from parent
     */
    @DeleteMapping("/{userId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> unlinkStudentFromParent(
        @PathVariable UUID userId,
        @PathVariable UUID studentId
    ) {
        parentService.unlinkStudentFromParent(userId, studentId);

        return ResponseEntity.ok(
            ApiResponse.success(
                Map.of("message", "Student unlinked from parent successfully")
            )
        );
    }
}
