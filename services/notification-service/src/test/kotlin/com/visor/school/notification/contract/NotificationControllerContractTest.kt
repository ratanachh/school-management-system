package com.visor.school.notification.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.visor.school.notification.controller.NotificationController
import com.visor.school.notification.service.NotificationService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@WebMvcTest(NotificationController::class)
class NotificationControllerContractTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var notificationService: NotificationService

    @Test
    fun `GET /api/v1/notifications should return notifications for user`() {
        // Given
        val userId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("userId", userId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
    }

    @Test
    fun `GET /api/v1/notifications should support unreadOnly filter`() {
        // Given
        val userId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("userId", userId.toString())
                .param("unreadOnly", "true")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `PUT /api/v1/notifications/{id}/read should mark notification as read`() {
        // Given
        val notificationId = UUID.randomUUID()

        // When & Then
        mockMvc.perform(
            put("/api/v1/notifications/$notificationId/read")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}

