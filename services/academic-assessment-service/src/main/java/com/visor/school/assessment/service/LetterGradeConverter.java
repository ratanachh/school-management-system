package com.visor.school.assessment.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Letter grade conversion service
 * Converts percentage scores to letter grades
 */
@Service
public class LetterGradeConverter {

    /**
     * Convert percentage to letter grade
     */
    public String convert(BigDecimal percentage) {
        if (percentage.compareTo(new BigDecimal("97.0")) >= 0) {
            return "A+";
        } else if (percentage.compareTo(new BigDecimal("93.0")) >= 0) {
            return "A";
        } else if (percentage.compareTo(new BigDecimal("90.0")) >= 0) {
            return "A-";
        } else if (percentage.compareTo(new BigDecimal("87.0")) >= 0) {
            return "B+";
        } else if (percentage.compareTo(new BigDecimal("83.0")) >= 0) {
            return "B";
        } else if (percentage.compareTo(new BigDecimal("80.0")) >= 0) {
            return "B-";
        } else if (percentage.compareTo(new BigDecimal("77.0")) >= 0) {
            return "C+";
        } else if (percentage.compareTo(new BigDecimal("73.0")) >= 0) {
            return "C";
        } else if (percentage.compareTo(new BigDecimal("70.0")) >= 0) {
            return "C-";
        } else if (percentage.compareTo(new BigDecimal("67.0")) >= 0) {
            return "D+";
        } else if (percentage.compareTo(new BigDecimal("63.0")) >= 0) {
            return "D";
        } else if (percentage.compareTo(new BigDecimal("60.0")) >= 0) {
            return "D-";
        } else {
            return "F";
        }
    }

    /**
     * Convert score and total points to letter grade
     */
    public String convert(BigDecimal score, BigDecimal totalPoints) {
        BigDecimal percentage;
        if (totalPoints.compareTo(BigDecimal.ZERO) > 0) {
            percentage = score.divide(totalPoints, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100.0"))
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            percentage = BigDecimal.ZERO;
        }
        return convert(percentage);
    }
}
