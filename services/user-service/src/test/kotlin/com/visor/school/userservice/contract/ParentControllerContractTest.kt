package com.visor.school.userservice.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.visor.school.userservice.controller.ParentController
import com.visor.school.userservice.model.Relationship
import com.visor.school.userservice.model.Parent
import com.visor.school.userservice.service.ParentService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ParentControllerContractTest {

    @Mock
    private lateinit var parentService: ParentService

    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        val parentController = ParentController(parentService)
        mockMvc = MockMvcBuilders.standaloneSetup(parentController).build()
    }

    @Test
    fun `GET api v1 parents parentId students should return parent's children`() {
        // Given
        val parentId = UUID.randomUUID()
        whenever(parentService.getChildrenByParent(parentId)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/v1/parents/{parentId}/students", parentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
    }

    @Test
    fun `POST api v1 parents userId students studentId should link student to parent`() {
        // Given
        val userId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        val request = mapOf(
            "relationship" to "MOTHER",
            "isPrimary" to true
        )
        val parent = Parent(
            userId = userId,
            studentId = studentId,
            relationship = Relationship.MOTHER,
            isPrimary = true
        )
        whenever(parentService.linkStudentToParent(userId, studentId, Relationship.MOTHER, true))
            .thenReturn(parent)

        // When & Then
        mockMvc.perform(
            post("/v1/parents/{userId}/students/{studentId}", userId, studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").exists())
    }

    @Test
    fun `DELETE api v1 parents userId students studentId should unlink student from parent`() {
        // Given
        val userId = UUID.randomUUID()
        val studentId = UUID.randomUUID()
        doNothing().whenever(parentService).unlinkStudentFromParent(userId, studentId)

        // When & Then
        mockMvc.perform(
            delete("/v1/parents/{userId}/students/{studentId}", userId, studentId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").exists())
    }
}

