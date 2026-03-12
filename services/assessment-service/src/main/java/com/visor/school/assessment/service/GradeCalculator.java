package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Grade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Grade calculation service
 */
@Service
public class GradeCalculator {

    /**
     * Calculate average grade from a list of grades
     */
    public BigDecimal calculateAverage(List<Grade> grades) {
        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPercentage = grades.stream()
            .map(Grade::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalPercentage.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate weighted average grade from a list of grades with weights
     */
    public BigDecimal calculateWeightedAverage(List<Grade> grades, Map<UUID, BigDecimal> weights) {
        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (Grade grade : grades) {
            BigDecimal weight = weights.getOrDefault(grade.getAssessmentId(), BigDecimal.ONE);
            totalWeightedScore = totalWeightedScore.add(grade.getPercentage().multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculate class average for an assessment
     */
    public BigDecimal calculateClassAverage(List<Grade> grades) {
        return calculateAverage(grades);
    }
}
