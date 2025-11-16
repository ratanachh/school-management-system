package com.visor.school.search.controller

import com.visor.school.search.model.SearchType
import com.visor.school.search.service.SearchService
import com.visor.school.common.api.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import jakarta.validation.constraints.NotBlank

/**
 * Search controller
 * Accessible by all authenticated users
 */
@RestController
@RequestMapping("/api/v1/search")
class SearchController(
    private val searchService: SearchService
) {

    /**
     * Search for students, teachers, or classes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun search(
        @RequestParam @NotBlank q: String,
        @RequestParam(required = false, defaultValue = "students") type: String
    ): ResponseEntity<ApiResponse<List<SearchResultResponse>>> {
        val searchType = when (type.lowercase()) {
            "students" -> SearchType.STUDENT
            "teachers" -> SearchType.TEACHER
            "classes" -> SearchType.CLASS
            else -> throw IllegalArgumentException("Invalid search type: $type. Must be one of: students, teachers, classes")
        }

        val results = searchService.search(q, searchType)
        val response = results.map { SearchResultResponse.from(it) }

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}

data class SearchResultResponse(
    val id: String,
    val type: String,
    val title: String,
    val content: String,
    val metadata: Map<String, Any>
) {
    companion object {
        fun from(index: com.visor.school.search.model.SearchIndex): SearchResultResponse {
            return SearchResultResponse(
                id = index.id.toString(),
                type = index.type.name,
                title = index.title,
                content = index.content,
                metadata = index.metadata
            )
        }
    }
}

