package com.visor.school.audit.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.audit.controller.AuditController;
import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.model.AuditRecord;
import com.visor.school.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = {AuditController.class},
    excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
public class AuditControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditService auditService;

    private UUID testUserId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        // Setup default mock responses
        when(auditService.query(any(), any(), any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void getApiV1AuditShouldReturnAuditRecords() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String action = "AUTHENTICATION";
        List<AuditRecord> records = List.of(
            new AuditRecord(
                userId,
                AuditAction.AUTHENTICATION,
                "User",
                userId.toString(),
                "192.168.1.1",
                "Mozilla/5.0",
                null,
                true,
                null
            )
        );
        when(auditService.query(userId, AuditAction.AUTHENTICATION, null, null))
            .thenReturn(records);

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("userId", userId.toString())
                .param("action", action)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void getApiV1AuditShouldSupportDateRangeFiltering() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";
        List<AuditRecord> records = List.of(
            new AuditRecord(
                userId,
                AuditAction.AUTHENTICATION,
                "User",
                userId.toString(),
                "192.168.1.1",
                "Mozilla/5.0",
                null,
                true,
                null
            )
        );
        when(auditService.query(userId, null, LocalDate.parse(startDate), LocalDate.parse(endDate)))
            .thenReturn(records);

        // When & Then
        mockMvc.perform(
            get("/api/v1/audit")
                .param("userId", userId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
