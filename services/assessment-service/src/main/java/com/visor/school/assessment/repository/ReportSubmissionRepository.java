package com.visor.school.assessment.repository;

import com.visor.school.assessment.model.ReportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportSubmissionRepository extends JpaRepository<ReportSubmission, UUID> {
    List<ReportSubmission> findByClassId(UUID classId);
    List<ReportSubmission> findBySubmittedBy(UUID submittedBy);
    List<ReportSubmission> findByCollectionId(UUID collectionId);
}
