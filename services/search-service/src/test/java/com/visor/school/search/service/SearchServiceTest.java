package com.visor.school.search.service;

import com.visor.school.search.integration.SearchIndexClient;
import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchIndexClient searchIndexClient;

    @InjectMocks
    private SearchService searchService;

    @BeforeEach
    void setup() {
        searchService = new SearchService(searchIndexClient);
    }

    @Test
    void shouldIndexDocument() {
        // Given
        SearchIndex searchIndex = SearchIndex.builder()
                .id(UUID.randomUUID())
                .type(SearchType.STUDENT)
                .title("John Doe")
                .content("Student in Grade 10")
                .metadata(Map.of("gradeLevel", "10", "studentId", "STU-001"))
                .build();
        doNothing().when(searchIndexClient).index(searchIndex);

        // When
        searchService.index(searchIndex);

        // Then
        verify(searchIndexClient).index(searchIndex);
    }

    @Test
    void shouldSearchDocuments() {
        // Given
        String query = "John Doe";
        SearchType type = SearchType.STUDENT;
        List<SearchIndex> results = Collections.singletonList(
                SearchIndex.builder()
                        .id(UUID.randomUUID())
                        .type(SearchType.STUDENT)
                        .title("John Doe")
                        .content("Student in Grade 10")
                        .metadata(Map.of("gradeLevel", "10"))
                        .build());
        when(searchIndexClient.search(query, type)).thenReturn(results);

        // When
        List<SearchIndex> found = searchService.search(query, type);

        // Then
        assertEquals(1, found.size());
        assertEquals("John Doe", found.get(0).getTitle());
        verify(searchIndexClient).search(query, type);
    }

    @Test
    void shouldUpdateIndex() {
        // Given
        SearchIndex searchIndex = SearchIndex.builder()
                .id(UUID.randomUUID())
                .type(SearchType.STUDENT)
                .title("John Doe Updated")
                .content("Student in Grade 11")
                .metadata(Map.of("gradeLevel", "11"))
                .build();
        doNothing().when(searchIndexClient).updateIndex(searchIndex);

        // When
        searchService.updateIndex(searchIndex);

        // Then
        verify(searchIndexClient).updateIndex(searchIndex);
    }

    @Test
    void shouldDeleteIndex() {
        // Given
        UUID id = UUID.randomUUID();
        SearchType type = SearchType.STUDENT;
        doNothing().when(searchIndexClient).deleteIndex(id, type);

        // When
        searchService.deleteIndex(id, type);

        // Then
        verify(searchIndexClient).deleteIndex(id, type);
    }
}
