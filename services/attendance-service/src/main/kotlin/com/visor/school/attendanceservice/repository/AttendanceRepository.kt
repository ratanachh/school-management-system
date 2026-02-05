package com.visor.school.attendanceservice.repository

import com.visor.school.attendanceservice.model.AttendanceRecord
import com.visor.school.attendanceservice.model.AttendanceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@Repository
interface AttendanceRepository : JpaRepository<AttendanceRecord, UUID> {
    fun findByStudentIdAndClassIdAndDate(studentId: UUID, classId: UUID, date: LocalDate): Optional<AttendanceRecord>
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId")
    fun findByClassId(@Param("classId") classId: UUID): List<AttendanceRecord>
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date = :date")
    fun findByClassIdAndDate(@Param("classId") classId: UUID, @Param("date") date: LocalDate): List<AttendanceRecord>
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.studentId = :studentId AND ar.classId = :classId")
    fun findByStudentIdAndClassId(@Param("studentId") studentId: UUID, @Param("classId") classId: UUID): List<AttendanceRecord>
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.sessionId = :sessionId")
    fun findBySessionId(@Param("sessionId") sessionId: UUID): List<AttendanceRecord>
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date BETWEEN :startDate AND :endDate")
    fun findByClassIdAndDateRange(
        @Param("classId") classId: UUID,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AttendanceRecord>
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date = :date AND ar.status = :status")
    fun countByClassIdAndDateAndStatus(
        @Param("classId") classId: UUID,
        @Param("date") date: LocalDate,
        @Param("status") status: AttendanceStatus
    ): Long
}

