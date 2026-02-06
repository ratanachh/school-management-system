package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.ExamResultCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamResultCollectionRepository extends JpaRepository<ExamResultCollection, UUID> {
    List<ExamResultCollection> findByClassIdAndAcademicYearAndTerm(
        UUID classId,
        String academicYear,
        String term
    );

    List<ExamResultCollection> findByCollectedBy(UUID collectedBy);
}
