package com.visor.school.assessment.service

import com.visor.school.assessment.model.Grade
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/**
 * Grade calculation service
 */
@Service
class GradeCalculator {

    /**
     * Calculate average grade from a list of grades
     */
    fun calculateAverage(grades: List<Grade>): BigDecimal {
        if (grades.isEmpty()) {
            return BigDecimal.ZERO
        }

        val totalPercentage = grades.sumOf { it.percentage }
        return totalPercentage.divide(BigDecimal(grades.size), 2, RoundingMode.HALF_UP)
    }

    /**
     * Calculate weighted average grade from a list of grades with weights
     */
    fun calculateWeightedAverage(grades: List<Grade>, weights: Map<UUID, BigDecimal>): BigDecimal {
        if (grades.isEmpty()) {
            return BigDecimal.ZERO
        }

        var totalWeightedScore = BigDecimal.ZERO
        var totalWeight = BigDecimal.ZERO

        grades.forEach { grade ->
            val weight = weights[grade.assessmentId] ?: BigDecimal.ONE
            totalWeightedScore += grade.percentage.multiply(weight)
            totalWeight += weight
        }

        return if (totalWeight > BigDecimal.ZERO) {
            totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }

    /**
     * Calculate class average for an assessment
     */
    fun calculateClassAverage(grades: List<Grade>): BigDecimal {
        return calculateAverage(grades)
    }
}

