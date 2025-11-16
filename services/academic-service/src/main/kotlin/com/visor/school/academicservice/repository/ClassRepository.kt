package com.visor.school.academicservice.repository

import com.visor.school.academicservice.model.Class
import com.visor.school.academicservice.model.ClassStatus
import com.visor.school.academicservice.model.ClassType
import com.visor.school.academicservice.model.Term
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClassRepository : JpaRepository<Class, UUID> {
    fun findByClassName(className: String): List<Class>
    fun findByGradeLevel(gradeLevel: Int): List<Class>
    fun findByClassType(classType: ClassType): List<Class>
    fun findByAcademicYearAndTerm(academicYear: String, term: Term): List<Class>
    
    @Query("SELECT c FROM Class c WHERE c.academicYear = :academicYear AND c.term = :term AND c.classType = :classType AND c.gradeLevel = :gradeLevel")
    fun findByAcademicYearAndTermAndTypeAndGrade(
        @Param("academicYear") academicYear: String,
        @Param("term") term: Term,
        @Param("classType") classType: ClassType,
        @Param("gradeLevel") gradeLevel: Int
    ): List<Class>
    
    @Query("SELECT c FROM Class c WHERE c.homeroomTeacherId = :teacherId")
    fun findByHomeroomTeacherId(@Param("teacherId") teacherId: UUID): List<Class>
    
    @Query("SELECT c FROM Class c WHERE c.classTeacherId = :teacherId")
    fun findByClassTeacherId(@Param("teacherId") teacherId: UUID): List<Class>
    
    fun findByStatus(status: ClassStatus): List<Class>
}

