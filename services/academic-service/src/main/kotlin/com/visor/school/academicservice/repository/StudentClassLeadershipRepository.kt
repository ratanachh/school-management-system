package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.LeadershipPosition
import com.visor.school.academicservice.model.StudentClassLeadership
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface StudentClassLeadershipRepository : JpaRepository<StudentClassLeadership, UUID> {
    fun findByStudentId(studentId: UUID): List<StudentClassLeadership>
    fun findByClassId(classId: UUID): List<StudentClassLeadership>
    
    @Query("SELECT scl FROM StudentClassLeadership scl WHERE scl.classId = :classId AND scl.leadershipPosition = :position")
    fun findByClassIdAndPosition(@Param("classId") classId: UUID, @Param("position") position: LeadershipPosition): Optional<StudentClassLeadership>
    
    @Query("SELECT scl FROM StudentClassLeadership scl WHERE scl.studentId = :studentId AND scl.classId = :classId")
    fun findByStudentIdAndClassId(@Param("studentId") studentId: UUID, @Param("classId") classId: UUID): Optional<StudentClassLeadership>
    
    fun existsByClassIdAndLeadershipPosition(classId: UUID, position: LeadershipPosition): Boolean
    fun existsByStudentIdAndClassId(studentId: UUID, classId: UUID): Boolean
}

