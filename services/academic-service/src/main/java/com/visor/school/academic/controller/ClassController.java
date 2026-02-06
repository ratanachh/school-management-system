package com.visor.school.academic.controller;

import com.visor.school.academic.model.ClassStatus;
import com.visor.school.academic.model.Schedule;
import com.visor.school.academic.model.Term;
import com.visor.school.academic.service.ClassService;
import com.visor.school.common.api.ApiResponse;
import static com.visor.school.academic.util.ApiResponseHelper.success;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class management controller
 * Requires ADMINISTRATOR role or MANAGE_HOMEROOM permission for homeroom classes
 */
@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    /**
     * Create a homeroom class (grades 1-6 only)
     */
    @PostMapping("/homeroom")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'MANAGE_HOMEROOM')")
    public ResponseEntity<ApiResponse<ClassResponse>> createHomeroomClass(@Valid @RequestBody CreateHomeroomClassRequest request) {
        Schedule schedule = null;
        if (request.schedule() != null) {
            ScheduleRequest schedReq = request.schedule();
            schedule = new Schedule(
                    schedReq.daysOfWeek(),
                    schedReq.startTime(),
                    schedReq.endTime(),
                    schedReq.room()
            );
        }

        com.visor.school.academic.model.Class classEntity = classService.createHomeroomClass(
                request.className(),
                request.gradeLevel(),
                request.homeroomTeacherId(),
                request.academicYear(),
                request.term(),
                schedule,
                request.maxCapacity(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(ClassResponse.from(classEntity), "Homeroom class created successfully"));
    }

    /**
     * Create a subject class (all grades)
     */
    @PostMapping("/subject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClassResponse>> createSubjectClass(@Valid @RequestBody CreateSubjectClassRequest request) {
        Schedule schedule = null;
        if (request.schedule() != null) {
            ScheduleRequest schedReq = request.schedule();
            schedule = new Schedule(
                    schedReq.daysOfWeek(),
                    schedReq.startTime(),
                    schedReq.endTime(),
                    schedReq.room()
            );
        }

        com.visor.school.academic.model.Class classEntity = classService.createSubjectClass(
                request.className(),
                request.subject(),
                request.gradeLevel(),
                request.academicYear(),
                request.term(),
                schedule,
                request.maxCapacity(),
                request.startDate(),
                request.endDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(ClassResponse.from(classEntity), "Subject class created successfully"));
    }

    /**
     * Assign class teacher/coordinator (grades 7-12 only)
     */
    @PostMapping("/{classId}/class-teacher")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClassResponse>> assignClassTeacher(
            @PathVariable UUID classId,
            @Valid @RequestBody AssignClassTeacherRequest request
    ) {
        com.visor.school.academic.model.Class classEntity = classService.assignClassTeacher(
                classId,
                request.teacherId(),
                request.assignedBy()
        );

        return ResponseEntity.ok(success(ClassResponse.from(classEntity), "Class teacher assigned successfully"));
    }

    /**
     * Get class by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<ClassResponse>> getClass(@PathVariable UUID id) {
        com.visor.school.academic.model.Class classEntity = classService.getClassById(id);
        if (classEntity == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(ClassResponse.from(classEntity)));
    }

    /**
     * Get classes by grade level
     */
    @GetMapping("/grade/{gradeLevel}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getClassesByGradeLevel(
            @PathVariable @Min(1) @Max(12) int gradeLevel
    ) {
        List<com.visor.school.academic.model.Class> classes = classService.getClassesByGradeLevel(gradeLevel);
        List<ClassResponse> responses = classes.stream()
                .map(ClassResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Update class status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClassStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClassStatusRequest request
    ) {
        com.visor.school.academic.model.Class classEntity = classService.updateClassStatus(id, request.status());
        return ResponseEntity.ok(success(ClassResponse.from(classEntity), "Class status updated"));
    }
}

record CreateHomeroomClassRequest(
        @NotBlank String className,
        @Min(1) @Max(6) int gradeLevel,
        @NotNull UUID homeroomTeacherId,
        @NotBlank String academicYear,
        @NotNull Term term,
        ScheduleRequest schedule,
        Integer maxCapacity,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}

record CreateSubjectClassRequest(
        @NotBlank String className,
        @NotBlank String subject,
        @Min(1) @Max(12) int gradeLevel,
        @NotBlank String academicYear,
        @NotNull Term term,
        ScheduleRequest schedule,
        Integer maxCapacity,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}

record AssignClassTeacherRequest(
        @NotNull UUID teacherId,
        UUID assignedBy
) {}

record UpdateClassStatusRequest(
        @NotNull ClassStatus status
) {}

record ScheduleRequest(
        @NotBlank String daysOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String room
) {}

record ClassResponse(
        UUID id,
        String className,
        String classType,
        String subject,
        int gradeLevel,
        UUID homeroomTeacherId,
        UUID classTeacherId,
        String academicYear,
        String term,
        Integer maxCapacity,
        int currentEnrollment,
        String status,
        LocalDate startDate,
        LocalDate endDate
) {
    public static ClassResponse from(com.visor.school.academic.model.Class classEntity) {
        return new ClassResponse(
                classEntity.getId(),
                classEntity.getClassName(),
                classEntity.getClassType().name(),
                classEntity.getSubject(),
                classEntity.getGradeLevel(),
                classEntity.getHomeroomTeacherId(),
                classEntity.getClassTeacherId(),
                classEntity.getAcademicYear(),
                classEntity.getTerm().name(),
                classEntity.getMaxCapacity(),
                classEntity.getCurrentEnrollment(),
                classEntity.getStatus().name(),
                classEntity.getStartDate(),
                classEntity.getEndDate()
        );
    }
}
