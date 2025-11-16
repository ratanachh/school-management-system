package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.AcademicRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface AcademicRecordRepository : JpaRepository<AcademicRecord, UUID> {
    fun findByStudentId(studentId: UUID): Optional<AcademicRecord>
}

