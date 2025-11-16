package com.visor.school.audit.repository

import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.model.AuditRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface AuditRepository : JpaRepository<AuditRecord, UUID> {
    fun findByUserId(userId: UUID): List<AuditRecord>
    fun findByAction(action: AuditAction): List<AuditRecord>
    
    @Query("SELECT a FROM AuditRecord a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate")
    fun findByUserIdAndTimestampBetween(
        @Param("userId") userId: UUID,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AuditRecord>
    
    @Query("SELECT a FROM AuditRecord a WHERE a.action = :action AND a.timestamp BETWEEN :startDate AND :endDate")
    fun findByActionAndTimestampBetween(
        @Param("action") action: AuditAction,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AuditRecord>
    
    @Query("SELECT a FROM AuditRecord a WHERE a.userId = :userId AND a.action = :action AND a.timestamp BETWEEN :startDate AND :endDate")
    fun findByUserIdAndActionAndTimestampBetween(
        @Param("userId") userId: UUID,
        @Param("action") action: AuditAction,
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<AuditRecord>
}

