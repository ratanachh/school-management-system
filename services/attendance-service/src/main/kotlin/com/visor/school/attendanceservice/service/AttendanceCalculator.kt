package com.visor.school.attendanceservice.service

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Service for calculating attendance rates and statistics
 */
@Service
class AttendanceCalculator {

    /**
     * Calculate attendance rate for a class on a specific date
     */
    fun calculateAttendanceRate(records: List<AttendanceRecord>): AttendanceRate {
        if (records.isEmpty()) {
            return AttendanceRate(0.0, 0, 0, 0, 0, 0)
        }

        val total = records.size
        val present = records.count { it.status == AttendanceStatus.PRESENT }
        val absent = records.count { it.status == AttendanceStatus.ABSENT }
        val late = records.count { it.status == AttendanceStatus.LATE }
        val excused = records.count { it.status == AttendanceStatus.EXCUSED }

        val rate = (present.toDouble() / total) * 100.0

        return AttendanceRate(rate, total, present, absent, late, excused)
    }

    /**
     * Calculate attendance rate for a date range
     */
    fun calculateAttendanceRateForRange(
        records: List<AttendanceRecord>,
        startDate: LocalDate,
        endDate: LocalDate
    ): AttendanceRate {
        val filteredRecords = records.filter { it.date.isAfter(startDate.minusDays(1)) && it.date.isBefore(endDate.plusDays(1)) }
        return calculateAttendanceRate(filteredRecords)
    }

    /**
     * Calculate student attendance rate
     */
    fun calculateStudentAttendanceRate(records: List<AttendanceRecord>): Double {
        if (records.isEmpty()) {
            return 0.0
        }

        val present = records.count { it.status == AttendanceStatus.PRESENT }
        return (present.toDouble() / records.size) * 100.0
    }
}

data class AttendanceRate(
    val rate: Double,
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val excused: Int
)

