package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByStudentId(UUID studentId);
    List<Grade> findByAssessmentId(UUID assessmentId);
    Optional<Grade> findByStudentIdAndAssessmentId(UUID studentId, UUID assessmentId);
    List<Grade> findByStudentIdAndAssessmentIdIn(UUID studentId, List<UUID> assessmentIds);
    
    @Query("SELECT g FROM Grade g JOIN Assessment a ON g.assessmentId = a.id WHERE a.classId = :classId")
    List<Grade> findByClassId(@Param("classId") UUID classId);
}
