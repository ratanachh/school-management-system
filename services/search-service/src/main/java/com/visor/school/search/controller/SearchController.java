package com.visor.school.search.controller;

import com.visor.school.common.api.ApiResponse;
import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import com.visor.school.search.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Search controller
 * Accessible by all authenticated users
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Search for students, teachers, or classes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> search(
            @RequestParam @NotBlank String q,
            @RequestParam(required = false, defaultValue = "students") String type) {
        SearchType searchType;
        switch (type.toLowerCase()) {
            case "students" -> searchType = SearchType.STUDENT;
            case "teachers" -> searchType = SearchType.TEACHER;
            case "classes" -> searchType = SearchType.CLASS;
            default -> throw new IllegalArgumentException(
                    "Invalid search type: " + type + ". Must be one of: students, teachers, classes");
        }

        List<SearchIndex> results = searchService.search(q, searchType);
        List<SearchResultResponse> response = results.stream()
                .map(SearchResultResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Data
    @Builder
    public static class SearchResultResponse {
        private String id;
        private String type;
        private String title;
        private String content;
        private Map<String, Object> metadata;

        public static SearchResultResponse from(SearchIndex index) {
            return SearchResultResponse.builder()
                    .id(index.getId().toString())
                    .type(index.getType().name())
                    .title(index.getTitle())
                    .content(index.getContent())
                    .metadata(index.getMetadata())
                    .build();
        }
    }
}
