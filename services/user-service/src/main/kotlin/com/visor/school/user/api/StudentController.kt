package com.visor.school.user.api

import com.visor.school.common.dto.ApiResponse
import com.visor.school.common.dto.PageResponse
import com.visor.school.user.domain.model.Student
import com.visor.school.user.service.StudentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/students")
class StudentController(
    private val studentService: StudentService
) {

    @GetMapping("/{id}")
    fun getStudentById(@PathVariable id: UUID): ResponseEntity<ApiResponse<Student>> {
        val student = studentService.findById(id)
        return ResponseEntity.ok(ApiResponse.success(student, "Student retrieved successfully"))
    }

    @GetMapping("/number/{studentNumber}")
    fun getStudentByNumber(@PathVariable studentNumber: String): ResponseEntity<ApiResponse<Student>> {
        val student = studentService.findByStudentNumber(studentNumber)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Student not found"))
        return ResponseEntity.ok(ApiResponse.success(student, "Student retrieved successfully"))
    }

    @GetMapping
    fun getAllStudents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<Student>>> {
        val pageResult = studentService.findAll(page, size)
        val pageResponse = PageResponse.of(pageResult.content, pageResult.number, pageResult.size, pageResult.totalElements)
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Students retrieved successfully"))
    }

    @GetMapping("/search")
    fun searchStudents(@RequestParam q: String): ResponseEntity<ApiResponse<List<Student>>> {
        val students = studentService.search(q)
        return ResponseEntity.ok(ApiResponse.success(students, "Search completed successfully"))
    }

    @PostMapping
    fun createStudent(@Valid @RequestBody student: Student): ResponseEntity<ApiResponse<Student>> {
        val createdStudent = studentService.create(student)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdStudent, "Student created successfully"))
    }

    @PutMapping("/{id}")
    fun updateStudent(@PathVariable id: UUID, @Valid @RequestBody student: Student): ResponseEntity<ApiResponse<Student>> {
        val updatedStudent = studentService.update(id, student)
        return ResponseEntity.ok(ApiResponse.success(updatedStudent, "Student updated successfully"))
    }

    @DeleteMapping("/{id}")
    fun deleteStudent(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        studentService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"))
    }
}

