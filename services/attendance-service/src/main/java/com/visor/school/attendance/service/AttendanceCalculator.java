package com.visor.school.attendance.service;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for calculating attendance rates and statistics
 */
@Service
public class AttendanceCalculator {

    /**
     * Calculate attendance rate for a class on a specific date
     */
    public AttendanceRate calculateAttendanceRate(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return new AttendanceRate(0.0, 0, 0, 0, 0, 0);
        }

        int total = records.size();
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long excused = records.stream().filter(r -> r.getStatus() == AttendanceStatus.EXCUSED).count();

        double rate = ((double) present / total) * 100.0;

        return new AttendanceRate(rate, total, (int) present, (int) absent, (int) late, (int) excused);
    }

    /**
     * Calculate attendance rate for a date range
     */
    public AttendanceRate calculateAttendanceRateForRange(
            List<AttendanceRecord> records,
            LocalDate startDate,
            LocalDate endDate) {
        List<AttendanceRecord> filteredRecords = records.stream()
            .filter(r -> r.getDate().isAfter(startDate.minusDays(1)) && r.getDate().isBefore(endDate.plusDays(1)))
            .toList();
        return calculateAttendanceRate(filteredRecords);
    }

    /**
     * Calculate student attendance rate
     */
    public double calculateStudentAttendanceRate(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return 0.0;
        }

        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        return ((double) present / records.size()) * 100.0;
    }

    public static class AttendanceRate {
        private final double rate;
        private final int total;
        private final int present;
        private final int absent;
        private final int late;
        private final int excused;

        public AttendanceRate(double rate, int total, int present, int absent, int late, int excused) {
            this.rate = rate;
            this.total = total;
            this.present = present;
            this.absent = absent;
            this.late = late;
            this.excused = excused;
        }

        public double getRate() {
            return rate;
        }

        public int getTotal() {
            return total;
        }

        public int getPresent() {
            return present;
        }

        public int getAbsent() {
            return absent;
        }

        public int getLate() {
            return late;
        }

        public int getExcused() {
            return excused;
        }
    }
}
