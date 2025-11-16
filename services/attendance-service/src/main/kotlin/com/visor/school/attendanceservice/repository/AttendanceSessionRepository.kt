package com.visor.school.attendanceservice.repository

import com.visor.school.attendanceservice.model.AttendanceSession
import com.visor.school.attendanceservice.model.AttendanceSessionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@Repository
interface AttendanceSessionRepository : JpaRepository<AttendanceSession, UUID> {
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId AND s.date = :date")
    fun findByClassIdAndDate(@Param("classId") classId: UUID, @Param("date") date: LocalDate): Optional<AttendanceSession>
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId")
    fun findByClassId(@Param("classId") classId: UUID): List<AttendanceSession>
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.delegatedTo = :studentId")
    fun findByDelegatedTo(@Param("studentId") studentId: UUID): List<AttendanceSession>
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.createdBy = :teacherId")
    fun findByCreatedBy(@Param("teacherId") teacherId: UUID): List<AttendanceSession>
    
    fun findByStatus(status: AttendanceSessionStatus): List<AttendanceSession>
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId AND s.status = :status")
    fun findByClassIdAndStatus(@Param("classId") classId: UUID, @Param("status") status: AttendanceSessionStatus): List<AttendanceSession>
}

