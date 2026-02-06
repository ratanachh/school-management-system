package com.visor.school.academic.integration;

import com.visor.school.academic.config.TestConfig;
import com.visor.school.academic.model.AcademicRecord;
import com.visor.school.academic.service.AcademicRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class TranscriptGenerationIntegrationTest {

    @Autowired
    private AcademicRecordService academicRecordService;

    @Test
    void shouldGenerateTranscriptForStudent() {
        UUID studentId = UUID.randomUUID();
        AcademicRecord record = academicRecordService.getOrCreateAcademicRecord(studentId);
        assertNotNull(record);

        byte[] transcript = academicRecordService.generateTranscript(studentId);
        assertNotNull(transcript);
        assertTrue(transcript.length > 0);
    }
}
