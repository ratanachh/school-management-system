package com.visor.school.academic.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.academic.controller.AcademicRecordController;
import com.visor.school.academic.model.AcademicRecord;
import com.visor.school.academic.model.AcademicStanding;
import com.visor.school.academic.service.AcademicRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademicRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class AcademicRecordControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AcademicRecordService academicRecordService;

    private final UUID studentId = UUID.randomUUID();

    @Test
    void shouldReturnAcademicRecordForValidStudentId() throws Exception {
        AcademicRecord academicRecord = new AcademicRecord(studentId, new BigDecimal("3.5"), new BigDecimal("3.5"), 60, 120, AcademicStanding.GOOD_STANDING);
        academicRecord.setId(UUID.randomUUID());

        when(academicRecordService.getAcademicRecord(studentId)).thenReturn(academicRecord);

        mockMvc.perform(get("/api/v1/academic-records/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(studentId.toString()));
    }

    @Test
    void shouldReturn404ForNonExistentStudentId() throws Exception {
        when(academicRecordService.getAcademicRecord(studentId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/academic-records/{studentId}", studentId))
                .andExpect(status().isNotFound());
    }
}
