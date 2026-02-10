package com.visor.school.userservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visor.school.userservice.model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByPermissionKey(String permissionKey);
    boolean existsByPermissionKey(String permissionKey);
    List<Permission> findByCategory(String category);
}
