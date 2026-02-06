package com.visor.school.audit.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AuditRecordTest {

    @Test
    public void shouldCreateAuditRecordWithRequiredFields() {
        AuditRecord record = new AuditRecord(
            UUID.randomUUID(),
            AuditAction.AUTHENTICATION,
            "User",
            UUID.randomUUID().toString(),
            "192.168.1.1",
            "Mozilla/5.0",
            null,
            true,
            null
        );

        assertNotNull(record.getId());
        assertNotNull(record.getUserId());
        assertEquals(AuditAction.AUTHENTICATION, record.getAction());
        assertEquals("User", record.getResourceType());
        assertNotNull(record.getTimestamp());
        assertEquals("192.168.1.1", record.getIpAddress());
        assertEquals("Mozilla/5.0", record.getUserAgent());
    }

    @Test
    public void shouldCreateAuditRecordWithAllFields() {
        AuditRecord record = new AuditRecord(
            UUID.randomUUID(),
            AuditAction.DATA_MODIFICATION,
            "Student",
            UUID.randomUUID().toString(),
            "192.168.1.1",
            "Mozilla/5.0",
            Map.of("field", "gradeLevel", "oldValue", "9", "newValue", "10"),
            true,
            null
        );

        assertEquals(AuditAction.DATA_MODIFICATION, record.getAction());
        assertEquals("Student", record.getResourceType());
        assertNotNull(record.getDetails());
        assertTrue(record.isSuccess());
        assertNull(record.getErrorMessage());
    }

    @Test
    public void shouldCreateAuditRecordForFailedAction() {
        AuditRecord record = new AuditRecord(
            UUID.randomUUID(),
            AuditAction.ACCESS_ATTEMPT,
            "Student",
            UUID.randomUUID().toString(),
            "192.168.1.1",
            "Mozilla/5.0",
            null,
            false,
            "Access denied: Insufficient permissions"
        );

        assertFalse(record.isSuccess());
        assertEquals("Access denied: Insufficient permissions", record.getErrorMessage());
    }

    @Test
    public void shouldAcceptAllAuditActionTypes() {
        List<AuditAction> actions = Arrays.asList(
            AuditAction.AUTHENTICATION,
            AuditAction.ACCESS_ATTEMPT,
            AuditAction.DATA_MODIFICATION,
            AuditAction.DATA_CREATION,
            AuditAction.DATA_DELETION,
            AuditAction.PASSWORD_RESET,
            AuditAction.EMAIL_VERIFICATION
        );

        actions.forEach(action -> {
            AuditRecord record = new AuditRecord(
                UUID.randomUUID(),
                action,
                "Test",
                UUID.randomUUID().toString(),
                "192.168.1.1",
                "Test",
                null,
                true,
                null
            );

            assertEquals(action, record.getAction());
        });
    }
}
