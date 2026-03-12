package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Grade;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class StudentGradebook {
    private final UUID studentId;
    private final List<Grade> grades;
    private final BigDecimal average;

    public StudentGradebook(UUID studentId, List<Grade> grades, BigDecimal average) {
        this.studentId = studentId;
        this.grades = grades;
        this.average = average;
    }

    public UUID getStudentId() { return studentId; }
    public List<Grade> getGrades() { return grades; }
    public BigDecimal getAverage() { return average; }
}
