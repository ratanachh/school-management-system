package com.visor.school.attendance.repository;

import com.visor.school.attendance.model.AttendanceRecord;
import com.visor.school.attendance.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findByStudentIdAndClassIdAndDate(UUID studentId, UUID classId, LocalDate date);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId")
    List<AttendanceRecord> findByClassId(@Param("classId") UUID classId);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date = :date")
    List<AttendanceRecord> findByClassIdAndDate(@Param("classId") UUID classId, @Param("date") LocalDate date);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.studentId = :studentId AND ar.classId = :classId")
    List<AttendanceRecord> findByStudentIdAndClassId(@Param("studentId") UUID studentId, @Param("classId") UUID classId);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.sessionId = :sessionId")
    List<AttendanceRecord> findBySessionId(@Param("sessionId") UUID sessionId);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByClassIdAndDateRange(
        @Param("classId") UUID classId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.classId = :classId AND ar.date = :date AND ar.status = :status")
    Long countByClassIdAndDateAndStatus(
        @Param("classId") UUID classId,
        @Param("date") LocalDate date,
        @Param("status") AttendanceStatus status
    );
}
