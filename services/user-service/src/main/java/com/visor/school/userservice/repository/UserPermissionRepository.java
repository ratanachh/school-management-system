package com.visor.school.userservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visor.school.userservice.model.User;
import com.visor.school.userservice.model.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    List<UserPermission> findByUser(User user);
    List<UserPermission> findByUserId(UUID userId);
    boolean existsByUserIdAndPermissionId(UUID userId, UUID permissionId);
    void deleteByUserIdAndPermissionId(UUID userId, UUID permissionId);
}
