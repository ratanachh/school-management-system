package com.visor.school.user.api

import com.visor.school.common.constant.Constants
import com.visor.school.common.dto.ApiResponse
import com.visor.school.common.dto.PageResponse
import com.visor.school.user.domain.model.User
import com.visor.school.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<ApiResponse<User>> {
        val user = userService.findById(id)
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"))
    }

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<ApiResponse<User>> {
        val user = userService.findByEmail(email)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"))
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"))
    }

    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "${Constants.DEFAULT_PAGE_SIZE}") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<User>>> {
        val pageResult = userService.findAll(page, size)
        val pageResponse = PageResponse.of(pageResult.content, pageResult.number, pageResult.size, pageResult.totalElements)
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Users retrieved successfully"))
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam q: String): ResponseEntity<ApiResponse<List<User>>> {
        val users = userService.search(q)
        return ResponseEntity.ok(ApiResponse.success(users, "Search completed successfully"))
    }

    @PostMapping
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<ApiResponse<User>> {
        val createdUser = userService.create(user)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdUser, "User created successfully"))
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: UUID, @Valid @RequestBody user: User): ResponseEntity<ApiResponse<User>> {
        val updatedUser = userService.update(id, user)
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"))
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        userService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"))
    }

    @PostMapping("/{id}/verify-email")
    fun verifyEmail(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        userService.verifyEmail(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"))
    }
}

