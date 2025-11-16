package com.visor.school.search.integration

import com.visor.school.search.model.SearchIndex
import com.visor.school.search.model.SearchType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

/**
 * Integration test for Elasticsearch client
 * Note: Requires Elasticsearch to be running
 */
@SpringBootTest
@ActiveProfiles("test")
class ElasticsearchClientTest @Autowired constructor(
    private val elasticsearchClient: ElasticsearchClient
) {

    @Test
    fun `should index and search document`() {
        // Given
        val searchIndex = SearchIndex(
            id = UUID.randomUUID(),
            type = SearchType.STUDENT,
            title = "Test Student",
            content = "Test student content",
            metadata = mapOf("gradeLevel" to "9")
        )

        // When - Index document
        elasticsearchClient.index(searchIndex)

        // Wait a bit for indexing
        Thread.sleep(1000)

        // Then - Search document
        val results = elasticsearchClient.search("Test Student", SearchType.STUDENT)

        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.title == "Test Student" })
    }

    @Test
    fun `should update indexed document`() {
        // Given
        val searchIndex = SearchIndex(
            id = UUID.randomUUID(),
            type = SearchType.STUDENT,
            title = "Original Title",
            content = "Original content",
            metadata = mapOf("gradeLevel" to "9")
        )
        elasticsearchClient.index(searchIndex)
        Thread.sleep(1000)

        // When - Update document
        val updatedIndex = searchIndex.copy(
            title = "Updated Title",
            content = "Updated content"
        )
        elasticsearchClient.updateIndex(updatedIndex)
        Thread.sleep(1000)

        // Then - Verify update
        val results = elasticsearchClient.search("Updated Title", SearchType.STUDENT)
        assertTrue(results.any { it.title == "Updated Title" })
    }

    @Test
    fun `should delete indexed document`() {
        // Given
        val searchIndex = SearchIndex(
            id = UUID.randomUUID(),
            type = SearchType.STUDENT,
            title = "To Be Deleted",
            content = "This will be deleted",
            metadata = mapOf("gradeLevel" to "9")
        )
        elasticsearchClient.index(searchIndex)
        Thread.sleep(1000)

        // When - Delete document
        elasticsearchClient.deleteIndex(searchIndex.id, SearchType.STUDENT)
        Thread.sleep(1000)

        // Then - Verify deletion
        val results = elasticsearchClient.search("To Be Deleted", SearchType.STUDENT)
        assertFalse(results.any { it.id == searchIndex.id })
    }
}

