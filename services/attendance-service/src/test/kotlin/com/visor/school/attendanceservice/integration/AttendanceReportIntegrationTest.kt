package com.visor.school.attendanceservice.integration

import com.visor.school.attendanceservice.model.AttendanceStatus
import com.visor.school.attendanceservice.repository.AttendanceRepository
import com.visor.school.attendanceservice.service.AttendanceService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Integration test for attendance report generation
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceReportIntegrationTest @Autowired constructor(
    private val attendanceService: AttendanceService,
    private val attendanceRepository: AttendanceRepository
) {

    private val testClassId = UUID.randomUUID()
    private val testTeacherId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        // Create attendance records for testing
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()

        // Create records for multiple days
        for (dayOffset in 0..7) {
            val date = startDate.plusDays(dayOffset.toLong())
            attendanceService.markAttendanceDirectly(
                studentId = UUID.randomUUID(),
                classId = testClassId,
                date = date,
                status = AttendanceStatus.PRESENT,
                markedBy = testTeacherId
            )
            attendanceService.markAttendanceDirectly(
                studentId = UUID.randomUUID(),
                classId = testClassId,
                date = date,
                status = AttendanceStatus.ABSENT,
                markedBy = testTeacherId
            )
        }
    }

    @Test
    fun `should generate attendance report for class`() {
        // Given
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()

        // When
        val report = attendanceService.generateClassReport(testClassId, startDate, endDate)

        // Then
        assertNotNull(report)
        assertEquals(testClassId, report.classId)
        assertEquals(startDate, report.startDate)
        assertEquals(endDate, report.endDate)
        assertNotNull(report.attendanceRate)
        assertTrue(report.attendanceRate >= 0.0)
        assertTrue(report.attendanceRate <= 1.0)
    }

    @Test
    fun `should calculate correct attendance statistics`() {
        // Given
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()

        // When
        val report = attendanceService.generateClassReport(testClassId, startDate, endDate)

        // Then
        assertNotNull(report.totalRecords)
        assertTrue(report.totalRecords > 0)
        assertNotNull(report.presentCount)
        assertNotNull(report.absentCount)
        assertNotNull(report.lateCount)
        assertNotNull(report.excusedCount)
    }

    @Test
    fun `should handle empty date range`() {
        // Given
        val futureStartDate = LocalDate.now().plusDays(30)
        val futureEndDate = LocalDate.now().plusDays(37)

        // When
        val report = attendanceService.generateClassReport(testClassId, futureStartDate, futureEndDate)

        // Then
        assertNotNull(report)
        assertEquals(0, report.totalRecords)
        assertEquals(0.0, report.attendanceRate)
    }
}

