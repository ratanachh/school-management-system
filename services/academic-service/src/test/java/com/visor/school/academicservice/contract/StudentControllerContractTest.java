package com.visor.school.academicservice.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visor.school.academicservice.controller.StudentController;
import com.visor.school.academicservice.model.EnrollmentStatus;
import com.visor.school.academicservice.model.Student;
import com.visor.school.academicservice.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    @Test
    void enrollStudentShouldReturn201() throws Exception {
        UUID userId = UUID.randomUUID();
        Map<String, Object> request = Map.of(
                "userId", userId.toString(),
                "firstName", "John",
                "lastName", "Doe",
                "dateOfBirth", "2010-01-01",
                "gradeLevel", 5
        );

        Student mockStudent = new Student("S12345", userId, "John", "Doe", LocalDate.of(2010, 1, 1), 5, EnrollmentStatus.ENROLLED, null, null);
        mockStudent.setId(UUID.randomUUID());

        when(studentService.enrollStudent(any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(mockStudent);

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void searchStudentsShouldReturnStudents() throws Exception {
        String searchQuery = "John";
        when(studentService.searchStudentsByName(searchQuery)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/students/search").param("name", searchQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getStudentByIdShouldReturnStudent() throws Exception {
        UUID studentId = UUID.randomUUID();
        Student mockStudent = new Student("S12345", UUID.randomUUID(), "John", "Doe", LocalDate.now(), 5, EnrollmentStatus.ENROLLED, null, null);
        mockStudent.setId(studentId);

        when(studentService.getStudentById(studentId)).thenReturn(mockStudent);

        mockMvc.perform(get("/api/v1/students/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStudentShouldReturn200() throws Exception {
        UUID studentId = UUID.randomUUID();
        Map<String, Object> request = Map.of("firstName", "Updated", "lastName", "Name", "gradeLevel", 6);

        Student mockStudent = new Student("S12345", UUID.randomUUID(), "Updated", "Name", LocalDate.of(2010, 1, 1), 6, EnrollmentStatus.ENROLLED, null, null);
        mockStudent.setId(studentId);

        when(studentService.updateStudent(any(), any(), any(), any(), any(), any())).thenReturn(mockStudent);

        mockMvc.perform(put("/api/v1/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void enrollStudentWithInvalidGradeShouldReject() throws Exception {
        Map<String, Object> request = Map.of(
                "userId", UUID.randomUUID().toString(),
                "firstName", "Invalid",
                "lastName", "Grade",
                "dateOfBirth", "2010-01-01",
                "gradeLevel", 13
        );

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudentsByGradeShouldReturnStudents() throws Exception {
        int gradeLevel = 5;
        when(studentService.getStudentsByGradeLevel(gradeLevel)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/students/grade/{gradeLevel}", gradeLevel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
