package com.visor.school.audit.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class AuditRecordTest {

    @Test
    fun `should create audit record with required fields`() {
        val record = AuditRecord(
            userId = UUID.randomUUID(),
            action = AuditAction.AUTHENTICATION,
            resourceType = "User",
            resourceId = UUID.randomUUID().toString(),
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0"
        )

        assertNotNull(record.id)
        assertNotNull(record.userId)
        assertEquals(AuditAction.AUTHENTICATION, record.action)
        assertEquals("User", record.resourceType)
        assertNotNull(record.timestamp)
        assertEquals("192.168.1.1", record.ipAddress)
        assertEquals("Mozilla/5.0", record.userAgent)
    }

    @Test
    fun `should create audit record with all fields`() {
        val record = AuditRecord(
            userId = UUID.randomUUID(),
            action = AuditAction.DATA_MODIFICATION,
            resourceType = "Student",
            resourceId = UUID.randomUUID().toString(),
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            details = mapOf("field" to "gradeLevel", "oldValue" to "9", "newValue" to "10"),
            success = true,
            errorMessage = null
        )

        assertEquals(AuditAction.DATA_MODIFICATION, record.action)
        assertEquals("Student", record.resourceType)
        assertNotNull(record.details)
        assertTrue(record.success)
        assertNull(record.errorMessage)
    }

    @Test
    fun `should create audit record for failed action`() {
        val record = AuditRecord(
            userId = UUID.randomUUID(),
            action = AuditAction.ACCESS_ATTEMPT,
            resourceType = "Student",
            resourceId = UUID.randomUUID().toString(),
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            success = false,
            errorMessage = "Access denied: Insufficient permissions"
        )

        assertFalse(record.success)
        assertEquals("Access denied: Insufficient permissions", record.errorMessage)
    }

    @Test
    fun `should accept all audit action types`() {
        val actions = listOf(
            AuditAction.AUTHENTICATION,
            AuditAction.ACCESS_ATTEMPT,
            AuditAction.DATA_MODIFICATION,
            AuditAction.DATA_CREATION,
            AuditAction.DATA_DELETION,
            AuditAction.PASSWORD_RESET,
            AuditAction.EMAIL_VERIFICATION
        )

        actions.forEach { action ->
            val record = AuditRecord(
                userId = UUID.randomUUID(),
                action = action,
                resourceType = "Test",
                resourceId = UUID.randomUUID().toString(),
                ipAddress = "192.168.1.1",
                userAgent = "Test"
            )

            assertEquals(action, record.action)
        }
    }
}

