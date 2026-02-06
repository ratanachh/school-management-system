package com.visor.school.attendance.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.attendance.controller.AttendanceController;
import com.visor.school.attendance.controller.AttendanceReportController;
import com.visor.school.attendance.controller.AttendanceSessionController;
import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.service.AttendanceCalculator;
import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.attendance.service.AttendanceService.AttendanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AttendanceController.class, AttendanceSessionController.class, AttendanceReportController.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "TEACHER")
class AttendanceControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setup() {
        when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
        when(permissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
    }

    @Test
    void postApiV1AttendanceShouldMarkAttendanceDirectlyAndReturn201() throws Exception {
        // Given
        UUID studentId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID markedBy = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", studentId.toString());
        request.put("classId", classId.toString());
        request.put("date", date.toString());
        request.put("status", AttendanceStatus.PRESENT.name());
        request.put("markedBy", markedBy.toString());

        AttendanceRecord record = new AttendanceRecord(studentId, classId, date, AttendanceStatus.PRESENT, markedBy);
        when(attendanceService.markAttendanceDirectly(eq(studentId), eq(classId), eq(date), eq(AttendanceStatus.PRESENT), eq(markedBy), any()))
            .thenReturn(record);

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void postApiV1AttendanceSessionsShouldCreateAttendanceSessionAndReturn201() throws Exception {
        // Given
        UUID classId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDate date = LocalDate.now();

        Map<String, Object> request = new HashMap<>();
        request.put("classId", classId.toString());
        request.put("date", date.toString());
        request.put("createdBy", createdBy.toString());

        AttendanceSession session = new AttendanceSession(classId, date, createdBy);
        when(attendanceService.createSession(any(), any(), any())).thenReturn(session);

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void postApiV1AttendanceSessionsSessionIdDelegateShouldDelegateToClassLeaderAndReturn200() throws Exception {
        // Given
        UUID sessionId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        Map<String, Object> request = new HashMap<>();
        request.put("classLeaderId", classLeaderId.toString());
        request.put("classId", classId.toString());

        AttendanceSession session = new AttendanceSession(classId, LocalDate.now(), UUID.randomUUID());
        session.delegateTo(classLeaderId);
        when(attendanceService.delegateToClassLeader(eq(sessionId), eq(classLeaderId), eq(classId)))
            .thenReturn(session);

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/delegate", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void postApiV1AttendanceSessionsSessionIdCollectShouldCollectAttendanceAndReturn200() throws Exception {
        // Given
        UUID sessionId = UUID.randomUUID();
        UUID classLeaderId = UUID.randomUUID();
        
        Map<String, Object> entry = new HashMap<>();
        entry.put("studentId", UUID.randomUUID().toString());
        entry.put("status", AttendanceStatus.PRESENT.name());
        entry.put("notes", null);

        Map<String, Object> request = new HashMap<>();
        request.put("classLeaderId", classLeaderId.toString());
        request.put("attendanceEntries", Collections.singletonList(entry));

        AttendanceSession session = new AttendanceSession(UUID.randomUUID(), LocalDate.now(), UUID.randomUUID());
        session.delegateTo(classLeaderId);
        session.markAsCollected();
        
        when(attendanceService.collectAttendanceByClassLeader(eq(sessionId), eq(classLeaderId), anyList()))
            .thenReturn(session);

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/collect", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void postApiV1AttendanceSessionsSessionIdApproveShouldApproveSessionAndReturn200() throws Exception {
        // Given
        UUID sessionId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        Map<String, String> request = new HashMap<>();
        request.put("teacherId", teacherId.toString());

        AttendanceSession session = new AttendanceSession(UUID.randomUUID(), LocalDate.now(), UUID.randomUUID());
        session.delegateTo(UUID.randomUUID());
        session.markAsCollected();
        session.approve(teacherId);
        
        when(attendanceService.approveSession(eq(sessionId), any(UUID.class))).thenReturn(session);

        // When & Then
        mockMvc.perform(
            post("/api/v1/attendance/sessions/{sessionId}/approve", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getApiV1AttendanceClassClassIdShouldReturnAttendanceRecords() throws Exception {
        // Given
        UUID classId = UUID.randomUUID();
        when(attendanceService.getAttendanceByClass(eq(classId), any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(
            get("/api/v1/attendance/class/{classId}", classId)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getApiV1AttendanceStudentStudentIdShouldReturnStudentAttendanceRecords() throws Exception {
        // Given
        UUID studentId = UUID.randomUUID();
        when(attendanceService.getAttendanceByStudent(eq(studentId), any())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(
            get("/api/v1/attendance/student/{studentId}", studentId)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getApiV1ReportsClassClassIdShouldReturnAttendanceReport() throws Exception {
        // Given
        UUID classId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        List<AttendanceRecord> records = Collections.singletonList(
             new AttendanceRecord(studentId, classId, startDate, AttendanceStatus.PRESENT, UUID.randomUUID())
        );
        AttendanceCalculator.AttendanceRate rate = new AttendanceCalculator().calculateAttendanceRateForRange(records, startDate, endDate);
        AttendanceReport report = new AttendanceReport(classId, startDate, endDate, 8, rate, records);

        when(attendanceService.generateClassReport(eq(classId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(report);

        // When & Then
        mockMvc.perform(
            get("/api/v1/reports/class/{classId}", classId)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists());
    }
}
