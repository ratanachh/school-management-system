package com.visor.school.attendance.repository;

import com.visor.school.attendance.model.AttendanceSession;
import com.visor.school.attendance.model.AttendanceSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId AND s.date = :date")
    Optional<AttendanceSession> findByClassIdAndDate(@Param("classId") UUID classId, @Param("date") LocalDate date);
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId")
    List<AttendanceSession> findByClassId(@Param("classId") UUID classId);
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.delegatedTo = :studentId")
    List<AttendanceSession> findByDelegatedTo(@Param("studentId") UUID studentId);
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.createdBy = :teacherId")
    List<AttendanceSession> findByCreatedBy(@Param("teacherId") UUID teacherId);
    
    List<AttendanceSession> findByStatus(AttendanceSessionStatus status);
    
    @Query("SELECT s FROM AttendanceSession s WHERE s.classId = :classId AND s.status = :status")
    List<AttendanceSession> findByClassIdAndStatus(@Param("classId") UUID classId, @Param("status") AttendanceSessionStatus status);
}
