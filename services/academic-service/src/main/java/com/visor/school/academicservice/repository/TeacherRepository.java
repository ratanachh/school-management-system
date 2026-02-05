package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.EmploymentStatus;
import com.visor.school.academicservice.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByUserId(UUID userId);
    List<Teacher> findByEmploymentStatus(EmploymentStatus status);
    
    @Query("SELECT t FROM Teacher t WHERE t.department = :department")
    List<Teacher> findByDepartment(@Param("department") String department);
}
