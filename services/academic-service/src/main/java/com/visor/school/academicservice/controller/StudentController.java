package com.visor.school.academicservice.controller;

import com.visor.school.academicservice.model.Address;
import com.visor.school.academicservice.model.EmergencyContact;
import com.visor.school.academicservice.model.EnrollmentStatus;
import com.visor.school.academicservice.model.Student;
import com.visor.school.academicservice.service.StudentService;
import com.visor.school.common.api.ApiResponse;
import static com.visor.school.academicservice.util.ApiResponseHelper.success;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Student management controller
 * Requires ADMINISTRATOR role or VIEW_ALL_STUDENTS permission for most operations
 */
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Enroll a new student
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<StudentResponse>> enrollStudent(@Valid @RequestBody EnrollStudentRequest request) {
        Address address = null;
        if (request.address() != null) {
            AddressRequest addrReq = request.address();
            address = new Address(
                    addrReq.street(),
                    addrReq.city(),
                    addrReq.state(),
                    addrReq.postalCode(),
                    addrReq.country() != null ? addrReq.country() : "Cambodia"
            );
        }

        EmergencyContact emergencyContact = null;
        if (request.emergencyContact() != null) {
            EmergencyContactRequest ecReq = request.emergencyContact();
            emergencyContact = new EmergencyContact(
                    ecReq.name(),
                    ecReq.relationship(),
                    ecReq.phoneNumber(),
                    ecReq.email(),
                    ecReq.address()
            );
        }

        Student student = studentService.enrollStudent(
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gradeLevel(),
                address,
                emergencyContact
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(StudentResponse.from(student), "Student enrolled successfully"));
    }

    /**
     * Get student by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable UUID id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(StudentResponse.from(student)));
    }

    /**
     * Get student by student ID
     */
    @GetMapping("/student-id/{studentId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentByStudentId(@PathVariable String studentId) {
        Student student = studentService.getStudentByStudentId(studentId);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(success(StudentResponse.from(student)));
    }

    /**
     * Search students by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> searchStudents(@RequestParam String name) {
        List<Student> students = studentService.searchStudentsByName(name);
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Get students by grade level
     */
    @GetMapping("/grade/{gradeLevel}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasPermission(null, 'VIEW_ALL_STUDENTS')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByGradeLevel(
            @PathVariable @Min(1) @Max(12) int gradeLevel
    ) {
        List<Student> students = studentService.getStudentsByGradeLevel(gradeLevel);
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(success(responses));
    }

    /**
     * Update student information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentRequest request
    ) {
        Address address = null;
        if (request.address() != null) {
            AddressRequest addrReq = request.address();
            address = new Address(
                    addrReq.street(),
                    addrReq.city(),
                    addrReq.state(),
                    addrReq.postalCode(),
                    addrReq.country() != null ? addrReq.country() : "Cambodia"
            );
        }

        EmergencyContact emergencyContact = null;
        if (request.emergencyContact() != null) {
            EmergencyContactRequest ecReq = request.emergencyContact();
            emergencyContact = new EmergencyContact(
                    ecReq.name(),
                    ecReq.relationship(),
                    ecReq.phoneNumber(),
                    ecReq.email(),
                    ecReq.address()
            );
        }

        Student student = studentService.updateStudent(
                id,
                request.firstName(),
                request.lastName(),
                request.gradeLevel(),
                address,
                emergencyContact
        );

        return ResponseEntity.ok(success(StudentResponse.from(student), "Student updated successfully"));
    }

    /**
     * Update enrollment status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateEnrollmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStudentStatusRequest request
    ) {
        Student student = studentService.updateEnrollmentStatus(id, request.status());
        return ResponseEntity.ok(success(StudentResponse.from(student), "Enrollment status updated"));
    }

    /**
     * Promote student to next grade
     */
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<StudentResponse>> promoteStudent(@PathVariable UUID id) {
        Student student = studentService.promoteStudent(id);
        return ResponseEntity.ok(success(StudentResponse.from(student), "Student promoted successfully"));
    }
}

// Request DTOs
record EnrollStudentRequest(
        @NotNull UUID userId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate dateOfBirth,
        @Min(1) @Max(12) int gradeLevel,
        AddressRequest address,
        EmergencyContactRequest emergencyContact
) {}

record UpdateStudentRequest(
        String firstName,
        String lastName,
        @Min(1) @Max(12) Integer gradeLevel,
        AddressRequest address,
        EmergencyContactRequest emergencyContact
) {}

record UpdateStudentStatusRequest(
        @NotNull EnrollmentStatus status
) {}

record AddressRequest(
        @NotBlank String street,
        @NotBlank String city,
        String state,
        @NotBlank String postalCode,
        String country
) {}

record EmergencyContactRequest(
        @NotBlank String name,
        @NotBlank String relationship,
        @NotBlank String phoneNumber,
        String email,
        String address
) {}

// Response DTOs
record StudentResponse(
        UUID id,
        String studentId,
        UUID userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        int gradeLevel,
        String enrollmentStatus,
        AddressRequest address,
        EmergencyContactRequest emergencyContact
) {
    public static StudentResponse from(Student student) {
        AddressRequest addressReq = null;
        if (student.getAddress() != null) {
            Address addr = student.getAddress();
            addressReq = new AddressRequest(
                    addr.getStreet(),
                    addr.getCity(),
                    addr.getState(),
                    addr.getPostalCode(),
                    addr.getCountry()
            );
        }

        EmergencyContactRequest emergencyContactReq = null;
        if (student.getEmergencyContact() != null) {
            EmergencyContact ec = student.getEmergencyContact();
            emergencyContactReq = new EmergencyContactRequest(
                    ec.getName(),
                    ec.getRelationship(),
                    ec.getPhoneNumber(),
                    ec.getEmail(),
                    ec.getAddress()
            );
        }

        return new StudentResponse(
                student.getId(),
                student.getStudentId(),
                student.getUserId(),
                student.getFirstName(),
                student.getLastName(),
                student.getDateOfBirth(),
                student.getGradeLevel(),
                student.getEnrollmentStatus().name(),
                addressReq,
                emergencyContactReq
        );
    }
}
