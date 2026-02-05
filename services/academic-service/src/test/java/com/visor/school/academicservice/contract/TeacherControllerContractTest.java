package com.visor.school.academicservice.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.academicservice.controller.TeacherController;
import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Teacher;
import com.visor.school.academicservice.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeacherService teacherService;

    @Test
    void createTeacherShouldReturn201() throws Exception {
        UUID userId = UUID.randomUUID();
        Map<String, Object> request = Map.of(
                "userId", userId.toString(),
                "qualifications", List.of("Bachelor's Degree"),
                "subjectSpecializations", List.of("Mathematics", "Physics"),
                "hireDate", "2020-01-01",
                "department", "Science"
        );

        Teacher mockTeacher = new Teacher("T12345", userId, List.of("Bachelor's Degree"), List.of("Mathematics", "Physics"), LocalDate.of(2020, 1, 1), "Science", EmploymentStatus.ACTIVE);
        mockTeacher.setId(UUID.randomUUID());

        when(teacherService.createTeacher(any(), any(), any(), any(), any())).thenReturn(mockTeacher);

        mockMvc.perform(post("/api/v1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getTeacherByIdShouldReturnTeacher() throws Exception {
        UUID teacherId = UUID.randomUUID();
        Teacher mockTeacher = new Teacher("T12345", UUID.randomUUID(), List.of("Bachelor's Degree"), List.of("Mathematics"), LocalDate.now(), "Science", EmploymentStatus.ACTIVE);
        mockTeacher.setId(teacherId);

        when(teacherService.getTeacherById(teacherId)).thenReturn(mockTeacher);

        mockMvc.perform(get("/api/v1/teachers/{id}", teacherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
