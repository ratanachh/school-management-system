package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.Assessment;
import com.visor.school.assessment.model.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findByClassId(UUID classId);
    List<Assessment> findByStatus(AssessmentStatus status);
    List<Assessment> findByClassIdAndStatus(UUID classId, AssessmentStatus status);
}
