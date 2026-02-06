package com.visor.school.attendance.integration;

import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.repository.AttendanceRepository;
import com.visor.school.attendance.service.AttendanceService;
import com.visor.school.attendance.service.AttendanceService.AttendanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for attendance report generation
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceReportIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private final UUID testClassId = UUID.randomUUID();
    private final UUID testTeacherId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        // Create attendance records for testing
        LocalDate startDate = LocalDate.now().minusDays(7);

        // Create records for multiple days
        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            LocalDate date = startDate.plusDays(dayOffset);
            attendanceService.markAttendanceDirectly(
                UUID.randomUUID(),
                testClassId,
                date,
                AttendanceStatus.PRESENT,
                testTeacherId,
                null
            );
            attendanceService.markAttendanceDirectly(
                UUID.randomUUID(),
                testClassId,
                date,
                AttendanceStatus.ABSENT,
                testTeacherId,
                null
            );
        }
    }

    @Test
    void shouldGenerateAttendanceReportForClass() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        // When
        AttendanceReport report = attendanceService.generateClassReport(testClassId, startDate, endDate);

        // Then
        assertNotNull(report);
        assertEquals(testClassId, report.getClassId());
        assertEquals(startDate, report.getStartDate());
        assertEquals(endDate, report.getEndDate());
        assertNotNull(report.getAttendanceRate());
        assertTrue(report.getAttendanceRate().getRate() >= 0.0);
        assertTrue(report.getAttendanceRate().getRate() <= 100.0);
    }

    @Test
    void shouldCalculateCorrectAttendanceStatistics() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        // When
        AttendanceReport report = attendanceService.generateClassReport(testClassId, startDate, endDate);

        // Then
        // Note: The Kotlin test accessed fields like report.totalRecords, report.presentCount.
        // These likely map to properties on AttendanceReport or AttendanceRate.
        // In my Java structure: report.getAttendanceRate().getTotal(), .getPresent(), etc.
        
        assertTrue(report.getAttendanceRate().getTotal() > 0);
        assertTrue(report.getAttendanceRate().getPresent() >= 0);
        assertTrue(report.getAttendanceRate().getAbsent() >= 0);
        assertTrue(report.getAttendanceRate().getLate() >= 0);
        assertTrue(report.getAttendanceRate().getExcused() >= 0);
    }

    @Test
    void shouldHandleEmptyDateRange() {
        // Given
        LocalDate futureStartDate = LocalDate.now().plusDays(30);
        LocalDate futureEndDate = LocalDate.now().plusDays(37);

        // When
        AttendanceReport report = attendanceService.generateClassReport(testClassId, futureStartDate, futureEndDate);

        // Then
        assertNotNull(report);
        assertEquals(0, report.getAttendanceRate().getTotal());
        assertEquals(0.0, report.getAttendanceRate().getRate());
    }
}
