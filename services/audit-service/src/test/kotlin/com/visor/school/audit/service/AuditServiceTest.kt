package com.visor.school.audit.service

import com.visor.school.audit.model.AuditAction
import com.visor.school.audit.model.AuditRecord
import com.visor.school.audit.repository.AuditRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AuditServiceTest {

    @Mock
    private lateinit var auditRepository: AuditRepository

    @InjectMocks
    private lateinit var auditService: AuditService

    private val testUserId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        auditService = AuditService(auditRepository)
    }

    @Test
    fun `should log audit record`() {
        // Given
        val record = AuditRecord(
            userId = testUserId,
            action = AuditAction.AUTHENTICATION,
            resourceType = "User",
            resourceId = testUserId.toString(),
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )
        whenever(auditRepository.save(any())).thenReturn(record)

        // When
        val result = auditService.log(
            userId = testUserId,
            action = AuditAction.AUTHENTICATION,
            resourceType = "User",
            resourceId = testUserId.toString(),
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        // Then
        assertNotNull(result)
        assertEquals(testUserId, result.userId)
        assertEquals(AuditAction.AUTHENTICATION, result.action)
        verify(auditRepository).save(any())
    }

    @Test
    fun `should query audit records`() {
        // Given
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val records = listOf(
            AuditRecord(
                userId = testUserId,
                action = AuditAction.AUTHENTICATION,
                resourceType = "User",
                resourceId = testUserId.toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditRepository.findByUserIdAndTimestampBetween(
            any(), any(), any()
        )).thenReturn(records)

        // When
        val result = auditService.query(
            userId = testUserId,
            action = AuditAction.AUTHENTICATION,
            startDate = startDate,
            endDate = endDate
        )

        // Then
        assertEquals(1, result.size)
        verify(auditRepository).findByUserIdAndTimestampBetween(any(), any(), any())
    }

    @Test
    fun `should get audit records by user`() {
        // Given
        val records = listOf(
            AuditRecord(
                userId = testUserId,
                action = AuditAction.AUTHENTICATION,
                resourceType = "User",
                resourceId = testUserId.toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditRepository.findByUserId(testUserId)).thenReturn(records)

        // When
        val result = auditService.getByUser(testUserId)

        // Then
        assertEquals(1, result.size)
        verify(auditRepository).findByUserId(testUserId)
    }

    @Test
    fun `should get audit records by action`() {
        // Given
        val records = listOf(
            AuditRecord(
                userId = testUserId,
                action = AuditAction.DATA_MODIFICATION,
                resourceType = "Student",
                resourceId = UUID.randomUUID().toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )
        )
        whenever(auditRepository.findByAction(AuditAction.DATA_MODIFICATION)).thenReturn(records)

        // When
        val result = auditService.getByAction(AuditAction.DATA_MODIFICATION)

        // Then
        assertEquals(1, result.size)
        assertEquals(AuditAction.DATA_MODIFICATION, result[0].action)
        verify(auditRepository).findByAction(AuditAction.DATA_MODIFICATION)
    }
}

