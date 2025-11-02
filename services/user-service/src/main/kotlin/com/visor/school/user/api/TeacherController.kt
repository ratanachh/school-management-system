package com.visor.school.user.api

import com.visor.school.common.dto.ApiResponse
import com.visor.school.common.dto.PageResponse
import com.visor.school.user.domain.model.Teacher
import com.visor.school.user.service.TeacherService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/teachers")
class TeacherController(
    private val teacherService: TeacherService
) {

    @GetMapping("/{id}")
    fun getTeacherById(@PathVariable id: UUID): ResponseEntity<ApiResponse<Teacher>> {
        val teacher = teacherService.findById(id)
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher retrieved successfully"))
    }

    @GetMapping("/number/{employeeNumber}")
    fun getTeacherByNumber(@PathVariable employeeNumber: String): ResponseEntity<ApiResponse<Teacher>> {
        val teacher = teacherService.findByEmployeeNumber(employeeNumber)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Teacher not found"))
        return ResponseEntity.ok(ApiResponse.success(teacher, "Teacher retrieved successfully"))
    }

    @GetMapping
    fun getAllTeachers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<Teacher>>> {
        val pageResult = teacherService.findAll(page, size)
        val pageResponse = PageResponse.of(pageResult.content, pageResult.number, pageResult.size, pageResult.totalElements)
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Teachers retrieved successfully"))
    }

    @GetMapping("/search")
    fun searchTeachers(@RequestParam q: String): ResponseEntity<ApiResponse<List<Teacher>>> {
        val teachers = teacherService.search(q)
        return ResponseEntity.ok(ApiResponse.success(teachers, "Search completed successfully"))
    }

    @PostMapping
    fun createTeacher(@Valid @RequestBody teacher: Teacher): ResponseEntity<ApiResponse<Teacher>> {
        val createdTeacher = teacherService.create(teacher)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdTeacher, "Teacher created successfully"))
    }

    @PutMapping("/{id}")
    fun updateTeacher(@PathVariable id: UUID, @Valid @RequestBody teacher: Teacher): ResponseEntity<ApiResponse<Teacher>> {
        val updatedTeacher = teacherService.update(id, teacher)
        return ResponseEntity.ok(ApiResponse.success(updatedTeacher, "Teacher updated successfully"))
    }

    @DeleteMapping("/{id}")
    fun deleteTeacher(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        teacherService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher deleted successfully"))
    }
}

