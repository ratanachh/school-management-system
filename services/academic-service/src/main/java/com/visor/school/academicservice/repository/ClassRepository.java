package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.Class;
import com.visor.school.academicservice.model.ClassStatus;
import com.visor.school.academicservice.model.ClassType;
import com.visor.school.academicservice.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<Class, UUID> {
    List<Class> findByClassName(String className);
    List<Class> findByGradeLevel(int gradeLevel);
    List<Class> findByClassType(ClassType classType);
    List<Class> findByAcademicYearAndTerm(String academicYear, Term term);
    
    @Query("SELECT c FROM Class c WHERE c.academicYear = :academicYear AND c.term = :term AND c.classType = :classType AND c.gradeLevel = :gradeLevel")
    List<Class> findByAcademicYearAndTermAndTypeAndGrade(
        @Param("academicYear") String academicYear,
        @Param("term") Term term,
        @Param("classType") ClassType classType,
        @Param("gradeLevel") int gradeLevel
    );
    
    @Query("SELECT c FROM Class c WHERE c.homeroomTeacherId = :teacherId")
    List<Class> findByHomeroomTeacherId(@Param("teacherId") UUID teacherId);
    
    @Query("SELECT c FROM Class c WHERE c.classTeacherId = :teacherId")
    List<Class> findByClassTeacherId(@Param("teacherId") UUID teacherId);
    
    List<Class> findByStatus(ClassStatus status);
}
