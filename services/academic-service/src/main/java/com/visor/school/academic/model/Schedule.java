package com.visor.school.academic.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schedule value object for class scheduling
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private String daysOfWeek; // Comma-separated days (e.g., "MONDAY,WEDNESDAY,FRIDAY")
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;

    public List<DayOfWeek> getDays() {
        return Arrays.stream(daysOfWeek.split(","))
            .map(day -> {
                try {
                    return DayOfWeek.valueOf(day.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(day -> day != null)
            .collect(Collectors.toList());
    }

    public int getDurationMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }
}
