package com.visor.school.audit.repository;

import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditRecord, UUID> {
    List<AuditRecord> findByUserId(UUID userId);
    List<AuditRecord> findByAction(AuditAction action);
    
    @Query("SELECT a FROM AuditRecord a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditRecord> findByUserIdAndTimestampBetween(
        @Param("userId") UUID userId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
    @Query("SELECT a FROM AuditRecord a WHERE a.action = :action AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditRecord> findByActionAndTimestampBetween(
        @Param("action") AuditAction action,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
    
    @Query("SELECT a FROM AuditRecord a WHERE a.userId = :userId AND a.action = :action AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditRecord> findByUserIdAndActionAndTimestampBetween(
        @Param("userId") UUID userId,
        @Param("action") AuditAction action,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
