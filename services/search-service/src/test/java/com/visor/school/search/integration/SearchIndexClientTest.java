package com.visor.school.search.integration;

import com.visor.school.search.model.SearchIndex;
import com.visor.school.search.model.SearchType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for Elasticsearch client
 * Note: Requires Elasticsearch to be running
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires running Elasticsearch")
class SearchIndexClientTest {

    @Autowired
    private SearchIndexClient searchIndexClient;

    @Test
    void shouldIndexAndSearchDocument() throws InterruptedException {
        // Given
        SearchIndex searchIndex = SearchIndex.builder()
                .id(UUID.randomUUID())
                .type(SearchType.STUDENT)
                .title("Test Student")
                .content("Test student content")
                .metadata(Map.of("gradeLevel", "9"))
                .build();

        // When - Index document
        searchIndexClient.index(searchIndex);

        // Wait a bit for indexing
        Thread.sleep(1000);

        // Then - Search document
        List<SearchIndex> results = searchIndexClient.search("Test Student", SearchType.STUDENT);

        assertTrue(!results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> "Test Student".equals(r.getTitle())));
    }

    @Test
    void shouldUpdateIndexedDocument() throws InterruptedException {
        // Given
        SearchIndex searchIndex = SearchIndex.builder()
                .id(UUID.randomUUID())
                .type(SearchType.STUDENT)
                .title("Original Title")
                .content("Original content")
                .metadata(Map.of("gradeLevel", "9"))
                .build();
        searchIndexClient.index(searchIndex);
        Thread.sleep(1000);

        // When - Update document
        SearchIndex updatedIndex = searchIndex.toBuilder()
                .title("Updated Title")
                .content("Updated content")
                .build();
        searchIndexClient.updateIndex(updatedIndex);
        Thread.sleep(1000);

        // Then - Verify update
        List<SearchIndex> results = searchIndexClient.search("Updated Title", SearchType.STUDENT);
        assertTrue(results.stream().anyMatch(r -> "Updated Title".equals(r.getTitle())));
    }

    @Test
    void shouldDeleteIndexedDocument() throws InterruptedException {
        // Given
        SearchIndex searchIndex = SearchIndex.builder()
                .id(UUID.randomUUID())
                .type(SearchType.STUDENT)
                .title("To Be Deleted")
                .content("This will be deleted")
                .metadata(Map.of("gradeLevel", "9"))
                .build();
        searchIndexClient.index(searchIndex);
        Thread.sleep(1000);

        // When - Delete document
        searchIndexClient.deleteIndex(searchIndex.getId(), SearchType.STUDENT);
        Thread.sleep(1000);

        // Then - Verify deletion
        List<SearchIndex> results = searchIndexClient.search("To Be Deleted", SearchType.STUDENT);
        assertFalse(results.stream().anyMatch(r -> r.getId().equals(searchIndex.getId())));
    }
}
