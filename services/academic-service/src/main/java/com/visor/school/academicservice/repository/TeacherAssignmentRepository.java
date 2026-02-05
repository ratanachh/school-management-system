package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, UUID> {
    List<TeacherAssignment> findByTeacherId(UUID teacherId);
    List<TeacherAssignment> findByClassId(UUID classId);
    
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.classId = :classId AND ta.isClassTeacher = true")
    List<TeacherAssignment> findClassTeacherByClassId(@Param("classId") UUID classId);
    
    boolean existsByTeacherIdAndClassId(UUID teacherId, UUID classId);
    
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId AND ta.classId = :classId")
    List<TeacherAssignment> findByTeacherIdAndClassId(@Param("teacherId") UUID teacherId, @Param("classId") UUID classId);
}
