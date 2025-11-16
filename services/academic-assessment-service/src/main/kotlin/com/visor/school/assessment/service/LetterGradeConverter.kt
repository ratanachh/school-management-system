package com.visor.school.assessment.service

import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Letter grade conversion service
 * Converts percentage scores to letter grades
 */
@Service
class LetterGradeConverter {

    /**
     * Convert percentage to letter grade
     */
    fun convert(percentage: BigDecimal): String {
        return when {
            percentage >= BigDecimal("97.0") -> "A+"
            percentage >= BigDecimal("93.0") -> "A"
            percentage >= BigDecimal("90.0") -> "A-"
            percentage >= BigDecimal("87.0") -> "B+"
            percentage >= BigDecimal("83.0") -> "B"
            percentage >= BigDecimal("80.0") -> "B-"
            percentage >= BigDecimal("77.0") -> "C+"
            percentage >= BigDecimal("73.0") -> "C"
            percentage >= BigDecimal("70.0") -> "C-"
            percentage >= BigDecimal("67.0") -> "D+"
            percentage >= BigDecimal("63.0") -> "D"
            percentage >= BigDecimal("60.0") -> "D-"
            else -> "F"
        }
    }

    /**
     * Convert score and total points to letter grade
     */
    fun convert(score: BigDecimal, totalPoints: BigDecimal): String {
        val percentage = if (totalPoints > BigDecimal.ZERO) {
            score.divide(totalPoints, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100.0"))
                .setScale(2, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
        return convert(percentage)
    }
}

