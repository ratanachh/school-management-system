package com.visor.school.academic.controller;

import com.visor.school.academic.model.EmploymentStatus;
import com.visor.school.academic.model.Teacher;
import com.visor.school.academic.service.TeacherService;
import com.visor.school.common.api.ApiResponse;
import static com.visor.school.academic.util.ApiResponseHelper.success;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Teacher management controller
 * Requires ADMINISTRATOR role for most operations
 */
@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /**
     * Create a new teacher
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        Teacher teacher = teacherService.createTeacher(
                request.userId(),
                request.qualifications(),
                request.subjectSpecializations(),
                request.hireDate(),
                request.department()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(TeacherResponse.from(teacher), "Teacher created successfully"));
    }

    /**
     * Get teacher by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacher(@PathVariable UUID id) {
        Teacher teacher = teacherService.getTeacherById(id);
        if (teacher == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(TeacherResponse.from(teacher)));
    }

    /**
     * Get teacher by user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByUserId(@PathVariable UUID userId) {
        Teacher teacher = teacherService.getTeacherByUserId(userId);
        if (teacher == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(TeacherResponse.from(teacher)));
    }

    /**
     * Get teachers by employment status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersByStatus(@PathVariable EmploymentStatus status) {
        List<Teacher> teachers = teacherService.getTeachersByStatus(status);
        List<TeacherResponse> responses = teachers.stream()
                .map(TeacherResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Get teachers by department
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersByDepartment(@PathVariable String department) {
        List<Teacher> teachers = teacherService.getTeachersByDepartment(department);
        List<TeacherResponse> responses = teachers.stream()
                .map(TeacherResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Update employment status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateEmploymentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTeacherStatusRequest request
    ) {
        Teacher teacher = teacherService.updateEmploymentStatus(id, request.status());
        return ResponseEntity.ok(success(TeacherResponse.from(teacher), "Employment status updated"));
    }
}

record CreateTeacherRequest(
        @NotNull UUID userId,
        List<String> qualifications,
        @NotEmpty List<String> subjectSpecializations,
        @NotNull LocalDate hireDate,
        String department
) {}

record UpdateTeacherStatusRequest(
        @NotNull EmploymentStatus status
) {}

record TeacherResponse(
        UUID id,
        String employeeId,
        UUID userId,
        List<String> qualifications,
        List<String> subjectSpecializations,
        LocalDate hireDate,
        String employmentStatus,
        String department
) {
    public static TeacherResponse from(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getEmployeeId(),
                teacher.getUserId(),
                teacher.getQualifications(),
                teacher.getSubjectSpecializations(),
                teacher.getHireDate(),
                teacher.getEmploymentStatus().name(),
                teacher.getDepartment()
        );
    }
}
