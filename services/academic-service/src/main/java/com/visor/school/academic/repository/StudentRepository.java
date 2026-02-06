package com.visor.school.academic.repository;

import com.visor.school.academic.model.EnrollmentStatus;
import com.visor.school.academic.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUserId(UUID userId);
    List<Student> findByGradeLevel(int gradeLevel);
    List<Student> findByEnrollmentStatus(EnrollmentStatus status);
    
    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name% OR CONCAT(s.firstName, ' ', s.lastName) LIKE %:name%")
    List<Student> searchByName(@Param("name") String name);
    
    @Query("SELECT s FROM Student s WHERE s.gradeLevel = :gradeLevel AND s.enrollmentStatus = :status")
    List<Student> findByGradeLevelAndStatus(@Param("gradeLevel") int gradeLevel, @Param("status") EnrollmentStatus status);
}
