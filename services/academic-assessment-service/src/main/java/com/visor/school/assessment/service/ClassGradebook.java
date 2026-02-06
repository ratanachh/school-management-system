package com.visor.school.assessment.service;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.Grade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClassGradebook {
    private final UUID classId;
    private final List<Assessment> assessments;
    private final Map<UUID, List<Grade>> studentGrades;
    private final Map<UUID, BigDecimal> studentAverages;

    public ClassGradebook(UUID classId, List<Assessment> assessments,
                         Map<UUID, List<Grade>> studentGrades,
                         Map<UUID, BigDecimal> studentAverages) {
        this.classId = classId;
        this.assessments = assessments;
        this.studentGrades = studentGrades;
        this.studentAverages = studentAverages;
    }

    public UUID getClassId() { return classId; }
    public List<Assessment> getAssessments() { return assessments; }
    public Map<UUID, List<Grade>> getStudentGrades() { return studentGrades; }
    public Map<UUID, BigDecimal> getStudentAverages() { return studentAverages; }
}
