package com.visor.school.academic.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.academic.controller.ClassController;
import com.visor.school.academic.model.ClassStatus;
import com.visor.school.academic.model.ClassType;
import com.visor.school.academic.model.Term;
import com.visor.school.academic.service.ClassService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClassController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClassControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClassService classService;

    @Test
    void createHomeroomClassShouldReturn201() throws Exception {
        UUID homeroomTeacherId = UUID.randomUUID();
        Map<String, Object> request = Map.of(
                "className", "Grade 3 Homeroom",
                "gradeLevel", 3,
                "homeroomTeacherId", homeroomTeacherId.toString(),
                "academicYear", "2024-2025",
                "term", Term.FIRST_TERM.name(),
                "startDate", "2024-09-01"
        );

        com.visor.school.academic.model.Class mockClass = new com.visor.school.academic.model.Class("Grade 3 Homeroom", ClassType.HOMEROOM, null, 3, homeroomTeacherId, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        mockClass.setId(UUID.randomUUID());

        when(classService.createHomeroomClass(any(), anyInt(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockClass);

        mockMvc.perform(post("/api/v1/classes/homeroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createHomeroomClassWithInvalidGradeShouldReject() throws Exception {
        Map<String, Object> request = Map.of(
                "className", "Invalid Homeroom",
                "gradeLevel", 7,
                "homeroomTeacherId", UUID.randomUUID().toString(),
                "academicYear", "2024-2025",
                "term", Term.FIRST_TERM.name(),
                "startDate", "2024-09-01"
        );

        mockMvc.perform(post("/api/v1/classes/homeroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
