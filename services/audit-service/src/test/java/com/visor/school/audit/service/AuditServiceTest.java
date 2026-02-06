package com.visor.school.audit.service;

import com.visor.school.audit.model.AuditAction;
import com.visor.school.audit.model.AuditRecord;
import com.visor.school.audit.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditService auditService;

    private UUID testUserId = UUID.randomUUID();

    @BeforeEach
    public void setup() {
        auditService = new AuditService(auditRepository);
    }

    @Test
    public void shouldLogAuditRecord() {
        // Given
        AuditRecord record = new AuditRecord(
            testUserId,
            AuditAction.AUTHENTICATION,
            "User",
            testUserId.toString(),
            "192.168.1.1",
            "Mozilla/5.0",
            null,
            true,
            null
        );
        when(auditRepository.save(any(AuditRecord.class))).thenReturn(record);

        // When
        AuditRecord result = auditService.log(
            testUserId,
            AuditAction.AUTHENTICATION,
            "User",
            testUserId.toString(),
            "192.168.1.1",
            "Mozilla/5.0",
            null,
            true,
            null
        );

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(AuditAction.AUTHENTICATION, result.getAction());
        verify(auditRepository).save(any(AuditRecord.class));
    }

    @Test
    public void shouldQueryAuditRecords() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<AuditRecord> records = List.of(
            new AuditRecord(
                testUserId,
                AuditAction.AUTHENTICATION,
                "User",
                testUserId.toString(),
                "192.168.1.1",
                "Mozilla/5.0",
                null,
                true,
                null
            )
        );
        when(auditRepository.findByUserIdAndActionAndTimestampBetween(
            any(), any(), any(), any()
        )).thenReturn(records);

        // When
        List<AuditRecord> result = auditService.query(
            testUserId,
            AuditAction.AUTHENTICATION,
            startDate,
            endDate
        );

        // Then
        assertEquals(1, result.size());
        verify(auditRepository).findByUserIdAndActionAndTimestampBetween(any(), any(), any(), any());
    }
}
