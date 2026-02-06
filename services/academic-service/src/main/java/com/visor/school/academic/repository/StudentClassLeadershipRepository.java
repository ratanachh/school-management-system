package com.visor.school.academic.repository;

import com.visor.school.academic.model.LeadershipPosition;
import com.visor.school.academic.model.StudentClassLeadership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentClassLeadershipRepository extends JpaRepository<StudentClassLeadership, UUID> {
    List<StudentClassLeadership> findByStudentId(UUID studentId);
    List<StudentClassLeadership> findByClassId(UUID classId);
    
    @Query("SELECT scl FROM StudentClassLeadership scl WHERE scl.classId = :classId AND scl.leadershipPosition = :position")
    Optional<StudentClassLeadership> findByClassIdAndPosition(@Param("classId") UUID classId, @Param("position") LeadershipPosition position);
    
    @Query("SELECT scl FROM StudentClassLeadership scl WHERE scl.studentId = :studentId AND scl.classId = :classId")
    Optional<StudentClassLeadership> findByStudentIdAndClassId(@Param("studentId") UUID studentId, @Param("classId") UUID classId);
    
    boolean existsByClassIdAndLeadershipPosition(UUID classId, LeadershipPosition position);
    boolean existsByStudentIdAndClassId(UUID studentId, UUID classId);
}
