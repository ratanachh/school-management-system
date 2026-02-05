package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findByStudentId(UUID studentId);
}
