package com.visor.school.notification.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.notification.controller.NotificationController;
import com.visor.school.notification.model.Notification;
import com.visor.school.notification.model.NotificationType;
import com.visor.school.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@WithMockUser
class NotificationControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldReturnNotificationsForUser() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("userId", userId.toString())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldSupportUnreadOnlyFilter() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(
            get("/api/v1/notifications")
                .param("userId", userId.toString())
                .param("unreadOnly", "true")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        // Given
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(
            UUID.randomUUID(),
            NotificationType.GRADE_POSTED,
            "Test",
            "Test Message",
            Collections.emptyMap()
        );
        notification.setId(notificationId);
        
        Mockito.when(notificationService.markAsRead(notificationId)).thenReturn(notification);

        // When & Then
        mockMvc.perform(
            put("/api/v1/notifications/" + notificationId + "/read")
                .with(csrf())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
