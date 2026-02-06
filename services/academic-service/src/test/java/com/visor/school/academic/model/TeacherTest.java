package com.visor.school.academic.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TeacherTest {
    @Test
    void shouldCreateTeacherWithRequiredFields() {
        Teacher teacher = new Teacher("EMP-2025-001", UUID.randomUUID(), List.of(), List.of("Mathematics", "Physics"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        assertNull(teacher.getId());
        assertEquals("EMP-2025-001", teacher.getEmployeeId());
        assertEquals(EmploymentStatus.ACTIVE, teacher.getEmploymentStatus());
        assertEquals(2, teacher.getSubjectSpecializations().size());
        assertTrue(teacher.getSubjectSpecializations().contains("Mathematics"));
    }
    @Test
    void shouldRequireAtLeastOneSubjectSpecialization() {
        assertThrows(IllegalArgumentException.class, () -> new Teacher("EMP-2025-002", UUID.randomUUID(), List.of(), List.of(), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE));
    }
    @Test
    void shouldAcceptMultipleSubjectSpecializations() {
        Teacher teacher = new Teacher("EMP-2025-003", UUID.randomUUID(), List.of(), List.of("Mathematics", "Physics", "Chemistry"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        assertEquals(3, teacher.getSubjectSpecializations().size());
    }
    @Test
    void shouldAcceptQualifications() {
        Teacher teacher = new Teacher("EMP-2025-004", UUID.randomUUID(), List.of("Bachelor's Degree", "Teaching Certificate"), List.of("English"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        assertEquals(2, teacher.getQualifications().size());
        assertTrue(teacher.getQualifications().contains("Bachelor's Degree"));
    }
    @Test
    void shouldUpdateEmploymentStatus() throws InterruptedException {
        Teacher teacher = new Teacher("EMP-2025-005", UUID.randomUUID(), List.of(), List.of("History"), LocalDate.of(2020, 1, 1), null, EmploymentStatus.ACTIVE);
        assertEquals(EmploymentStatus.ACTIVE, teacher.getEmploymentStatus());
        var initialUpdatedAt = teacher.getUpdatedAt();
        Thread.sleep(10);
        teacher.updateEmploymentStatus(EmploymentStatus.ON_LEAVE);
        assertEquals(EmploymentStatus.ON_LEAVE, teacher.getEmploymentStatus());
        assertTrue(teacher.getUpdatedAt().isAfter(initialUpdatedAt));
    }
    @Test
    void shouldAcceptAllEmploymentStatuses() {
        List<EmploymentStatus> statuses = List.of(EmploymentStatus.ACTIVE, EmploymentStatus.ON_LEAVE, EmploymentStatus.TERMINATED, EmploymentStatus.RETIRED);
        for (EmploymentStatus status : statuses) {
            Teacher teacher = new Teacher("EMP-" + status.name(), UUID.randomUUID(), List.of(), List.of("Test"), LocalDate.of(2020, 1, 1), null, status);
            assertEquals(status, teacher.getEmploymentStatus());
        }
    }
    @Test
    void shouldAcceptDepartmentAssignment() {
        Teacher teacher = new Teacher("EMP-2025-006", UUID.randomUUID(), List.of(), List.of("Science"), LocalDate.of(2020, 1, 1), "Science Department", EmploymentStatus.ACTIVE);
        assertEquals("Science Department", teacher.getDepartment());
    }
}
