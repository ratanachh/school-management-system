package com.visor.school.userservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visor.school.userservice.model.Parent;

@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID> {
    List<Parent> findByUserId(UUID userId);
    List<Parent> findByStudentId(UUID studentId);
    boolean existsByUserIdAndStudentId(UUID userId, UUID studentId);
}
