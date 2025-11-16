package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.TeacherAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeacherAssignmentRepository : JpaRepository<TeacherAssignment, UUID> {
    fun findByTeacherId(teacherId: UUID): List<TeacherAssignment>
    fun findByClassId(classId: UUID): List<TeacherAssignment>
    
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.classId = :classId AND ta.isClassTeacher = true")
    fun findClassTeacherByClassId(@Param("classId") classId: UUID): List<TeacherAssignment>
    
    fun existsByTeacherIdAndClassId(teacherId: UUID, classId: UUID): Boolean
    
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacherId = :teacherId AND ta.classId = :classId")
    fun findByTeacherIdAndClassId(@Param("teacherId") teacherId: UUID, @Param("classId") classId: UUID): List<TeacherAssignment>
}

