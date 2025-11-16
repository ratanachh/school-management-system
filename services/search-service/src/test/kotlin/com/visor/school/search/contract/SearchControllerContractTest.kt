package com.visor.school.search.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.search.controller.SearchController
import com.visor.school.search.service.SearchService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(SearchController::class)
class SearchControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var searchService: SearchService

    @Test
    fun `GET /api/v1/search should return search results`() {
        // Given
        val query = "John"
        val type = "students"

        // When & Then
        mockMvc.perform(
            get("/api/v1/search")
                .param("q", query)
                .param("type", type)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/search should support teachers type`() {
        // Given
        val query = "Smith"
        val type = "teachers"

        // When & Then
        mockMvc.perform(
            get("/api/v1/search")
                .param("q", query)
                .param("type", type)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `GET /api/v1/search should support classes type`() {
        // Given
        val query = "Mathematics"
        val type = "classes"

        // When & Then
        mockMvc.perform(
            get("/api/v1/search")
                .param("q", query)
                .param("type", type)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}

