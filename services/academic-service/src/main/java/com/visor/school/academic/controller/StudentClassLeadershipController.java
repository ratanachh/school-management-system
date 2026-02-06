package com.visor.school.academic.controller;

import com.visor.school.academic.model.LeadershipPosition;
import com.visor.school.academic.model.StudentClassLeadership;
import com.visor.school.academic.service.StudentClassLeadershipService;
import com.visor.school.common.api.ApiResponse;
import static com.visor.school.academic.util.ApiResponseHelper.success;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Student class leadership controller
 * Requires ADMINISTRATOR role or class teacher permissions
 */
@RestController
@RequestMapping("/api/v1/classes/{classId}/leaders")
public class StudentClassLeadershipController {

    private final StudentClassLeadershipService leadershipService;

    public StudentClassLeadershipController(StudentClassLeadershipService leadershipService) {
        this.leadershipService = leadershipService;
    }

    /**
     * Assign class leader to a class
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<StudentClassLeadershipResponse>> assignClassLeader(
            @PathVariable UUID classId,
            @Valid @RequestBody AssignLeaderRequest request
    ) {
        StudentClassLeadership leadership = leadershipService.assignLeader(
                request.studentId(),
                classId,
                request.position(),
                request.assignedBy() // From JWT token in production
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(StudentClassLeadershipResponse.from(leadership), "Class leader assigned successfully"));
    }

    /**
     * Get all class leaders for a class
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentClassLeadershipResponse>>> getClassLeaders(@PathVariable UUID classId) {
        List<StudentClassLeadership> leaders = leadershipService.getLeadersByClass(classId);
        List<StudentClassLeadershipResponse> responses = leaders.stream()
                .map(StudentClassLeadershipResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Get class leader by position
     */
    @GetMapping("/position/{position}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentClassLeadershipResponse>> getLeaderByPosition(
            @PathVariable UUID classId,
            @PathVariable LeadershipPosition position
    ) {
        StudentClassLeadership leader = leadershipService.getLeaderByPosition(classId, position);
        if (leader == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(StudentClassLeadershipResponse.from(leader)));
    }

    /**
     * Check if student is a class leader for the class
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkClassLeader(
            @PathVariable UUID classId,
            @PathVariable UUID studentId
    ) {
        boolean isLeader = leadershipService.isClassLeader(studentId, classId);
        return ResponseEntity.ok(
                success(
                        Map.of(
                                "isClassLeader", isLeader,
                                "studentId", studentId.toString(),
                                "classId", classId.toString()
                        )
                )
        );
    }

    /**
     * Remove class leader assignment
     */
    @DeleteMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Map<String, String>>> removeClassLeader(
            @PathVariable UUID classId,
            @PathVariable UUID studentId
    ) {
        leadershipService.removeLeader(studentId, classId);
        return ResponseEntity.ok(
                success(
                        Map.of("message", "Class leader assignment removed successfully"),
                        null
                )
        );
    }
}

record AssignLeaderRequest(
        @NotNull UUID studentId,
        @NotNull LeadershipPosition position,
        @NotNull UUID assignedBy // From JWT token in production
) {}

record StudentClassLeadershipResponse(
        UUID id,
        UUID studentId,
        UUID classId,
        String leadershipPosition,
        UUID assignedBy,
        Instant assignedAt
) {
    public static StudentClassLeadershipResponse from(StudentClassLeadership leadership) {
        return new StudentClassLeadershipResponse(
                leadership.getId(),
                leadership.getStudentId(),
                leadership.getClassId(),
                leadership.getLeadershipPosition().name(),
                leadership.getAssignedBy(),
                leadership.getAssignedAt()
        );
    }
}
