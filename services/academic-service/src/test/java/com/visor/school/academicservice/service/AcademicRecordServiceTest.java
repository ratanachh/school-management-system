package com.visor.school.academicservice.service;

import com.visor.school.academicservice.event.AcademicRecordEventPublisher;
import com.visor.school.academicservice.model.AcademicRecord;
import com.visor.school.academicservice.model.AcademicStanding;
import com.visor.school.academicservice.repository.AcademicRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicRecordServiceTest {

    @Mock
    private AcademicRecordRepository academicRecordRepository;

    @Mock
    private GPACalculator gpaCalculator;

    @Mock
    private TranscriptGenerator transcriptGenerator;

    @Mock
    private AcademicRecordEventPublisher academicRecordEventPublisher;

    @InjectMocks
    private AcademicRecordService academicRecordService;

    private final UUID testStudentId = UUID.randomUUID();

    @Test
    void shouldGetAcademicRecordByStudentId() {
        AcademicRecord record = new AcademicRecord(testStudentId, new BigDecimal("3.4"), new BigDecimal("3.4"), 24, 120, AcademicStanding.GOOD_STANDING);
        when(academicRecordRepository.findByStudentId(testStudentId)).thenReturn(Optional.of(record));

        AcademicRecord result = academicRecordService.getAcademicRecord(testStudentId);

        assertNotNull(result);
        assertEquals(testStudentId, result.getStudentId());
        assertEquals(new BigDecimal("3.4"), result.getCumulativeGPA());
        verify(academicRecordRepository).findByStudentId(testStudentId);
    }

    @Test
    void shouldReturnNullWhenRecordNotFound() {
        when(academicRecordRepository.findByStudentId(testStudentId)).thenReturn(Optional.empty());

        AcademicRecord result = academicRecordService.getAcademicRecord(testStudentId);

        assertNull(result);
    }

    @Test
    void shouldCreateAcademicRecordIfNotExists() {
        when(academicRecordRepository.findByStudentId(testStudentId)).thenReturn(Optional.empty());
        when(academicRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        AcademicRecord result = academicRecordService.getOrCreateAcademicRecord(testStudentId);

        assertNotNull(result);
        assertEquals(testStudentId, result.getStudentId());
        assertEquals(BigDecimal.ZERO, result.getCumulativeGPA());
        verify(academicRecordRepository).save(any());
    }
}
