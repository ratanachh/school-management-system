package com.visor.school.attendance.service;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import com.visor.school.attendance.service.AttendanceCalculator.AttendanceRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendanceCalculatorTest {

    private AttendanceCalculator calculator;

    @BeforeEach
    void setup() {
        calculator = new AttendanceCalculator();
    }

    @Test
    void shouldCalculateRateForEmptyRecords() {
        AttendanceRate result = calculator.calculateAttendanceRate(Collections.emptyList());

        assertEquals(0.0, result.getRate());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getPresent());
        assertEquals(0, result.getAbsent());
        assertEquals(0, result.getLate());
        assertEquals(0, result.getExcused());
    }

    @Test
    void shouldCalculateRateCorrectly() {
        List<AttendanceRecord> records = new ArrayList<>();
        // 5 PRESENT, 2 ABSENT, 1 LATE, 2 EXCUSED = 10 Total
        addRecords(records, AttendanceStatus.PRESENT, 5);
        addRecords(records, AttendanceStatus.ABSENT, 2);
        addRecords(records, AttendanceStatus.LATE, 1);
        addRecords(records, AttendanceStatus.EXCUSED, 2);

        AttendanceRate result = calculator.calculateAttendanceRate(records);

        assertEquals(50.0, result.getRate());
        assertEquals(10, result.getTotal());
        assertEquals(5, result.getPresent());
        assertEquals(2, result.getAbsent());
        assertEquals(1, result.getLate());
        assertEquals(2, result.getExcused());
    }

    @Test
    void shouldCalculateRateForRange() {
        LocalDate today = LocalDate.now();
        List<AttendanceRecord> records = new ArrayList<>();
        
        LocalDate start = today.minusDays(5);
        LocalDate end = today.minusDays(2);
        
        // Inside range [today-5, today-2]
        records.add(createRecord(today.minusDays(3), AttendanceStatus.PRESENT));
        records.add(createRecord(today.minusDays(4), AttendanceStatus.ABSENT));
        
        // Outside range (Before)
        records.add(createRecord(today.minusDays(6), AttendanceStatus.PRESENT));
        
        // Outside range (After)
        records.add(createRecord(today.minusDays(1), AttendanceStatus.PRESENT));

        AttendanceRate result = calculator.calculateAttendanceRateForRange(
            records, 
            start, 
            end
        );

        assertEquals(50.0, result.getRate());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getPresent());
        assertEquals(1, result.getAbsent());
    }

    @Test
    void shouldCalculateStudentAttendanceRate() {
        List<AttendanceRecord> records = new ArrayList<>();
        addRecords(records, AttendanceStatus.PRESENT, 3); // 3
        addRecords(records, AttendanceStatus.ABSENT, 1); // 1
        // Total 4. Rate = 3/4 = 75%

        double rate = calculator.calculateStudentAttendanceRate(records);

        assertEquals(75.0, rate);
    }

    @Test
    void shouldCalculateStudentAttendanceRateEmpty() {
        double rate = calculator.calculateStudentAttendanceRate(Collections.emptyList());
        assertEquals(0.0, rate);
    }

    private void addRecords(List<AttendanceRecord> records, AttendanceStatus status, int count) {
        for (int i = 0; i < count; i++) {
            records.add(createRecord(LocalDate.now(), status));
        }
    }

    private AttendanceRecord createRecord(LocalDate date, AttendanceStatus status) {
        // Create record without invoking constructor that validates fields strictly?
        // Actually the protected constructor + reflection is easiest, OR use a public one if available.
        // There is a protected constructor for JPA.
        // I can use Reflection or minimal public constructor.
        // Public constructors validate many things.
        // Let's use Reflection to instantiate via private/protected no-arg constructor if possible
        // or just use the public one with dummy UUIDs.
        
        // Public constructor: AttendanceRecord(UUID studentId, UUID classId, LocalDate date, AttendanceStatus status, UUID markedBy)
        return new AttendanceRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            date,
            status,
            UUID.randomUUID()
        );
    }
}
