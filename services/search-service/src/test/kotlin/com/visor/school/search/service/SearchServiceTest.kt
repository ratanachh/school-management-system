package com.visor.school.search.service

import com.visor.school.search.integration.ElasticsearchClient
import com.visor.school.search.model.SearchIndex
import com.visor.school.search.model.SearchType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SearchServiceTest {

    @Mock
    private lateinit var elasticsearchClient: ElasticsearchClient

    @InjectMocks
    private lateinit var searchService: SearchService

    @BeforeEach
    fun setup() {
        searchService = SearchService(elasticsearchClient)
    }

    @Test
    fun `should index document`() {
        // Given
        val searchIndex = SearchIndex(
            id = UUID.randomUUID(),
            type = SearchType.STUDENT,
            title = "John Doe",
            content = "Student in Grade 10",
            metadata = mapOf("gradeLevel" to "10", "studentId" to "STU-001")
        )
        doNothing().whenever(elasticsearchClient).index(searchIndex)

        // When
        searchService.index(searchIndex)

        // Then
        verify(elasticsearchClient).index(searchIndex)
    }

    @Test
    fun `should search documents`() {
        // Given
        val query = "John Doe"
        val type = SearchType.STUDENT
        val results = listOf(
            SearchIndex(
                id = UUID.randomUUID(),
                type = SearchType.STUDENT,
                title = "John Doe",
                content = "Student in Grade 10",
                metadata = mapOf("gradeLevel" to "10")
            )
        )
        whenever(elasticsearchClient.search(query, type)).thenReturn(results)

        // When
        val found = searchService.search(query, type)

        // Then
        assertEquals(1, found.size)
        assertEquals("John Doe", found[0].title)
        verify(elasticsearchClient).search(query, type)
    }

    @Test
    fun `should update index`() {
        // Given
        val searchIndex = SearchIndex(
            id = UUID.randomUUID(),
            type = SearchType.STUDENT,
            title = "John Doe Updated",
            content = "Student in Grade 11",
            metadata = mapOf("gradeLevel" to "11")
        )
        doNothing().whenever(elasticsearchClient).updateIndex(searchIndex)

        // When
        searchService.updateIndex(searchIndex)

        // Then
        verify(elasticsearchClient).updateIndex(searchIndex)
    }

    @Test
    fun `should delete index`() {
        // Given
        val id = UUID.randomUUID()
        val type = SearchType.STUDENT
        doNothing().whenever(elasticsearchClient).deleteIndex(id, type)

        // When
        searchService.deleteIndex(id, type)

        // Then
        verify(elasticsearchClient).deleteIndex(id, type)
    }
}

