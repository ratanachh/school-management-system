package com.visor.school.academicservice.repository;

import com.visor.school.academicservice.model.ClassStatus;
import com.visor.school.academicservice.model.ClassType;
import com.visor.school.academicservice.model.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ClassRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClassRepository classRepository;

    private com.visor.school.academicservice.model.Class testHomeroomClass;
    private com.visor.school.academicservice.model.Class testSubjectClass;

    @BeforeEach
    void setup() {
        testHomeroomClass = new com.visor.school.academicservice.model.Class("Grade 3 Homeroom", ClassType.HOMEROOM, null, 3, UUID.randomUUID(), null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        entityManager.persistAndFlush(testHomeroomClass);

        testSubjectClass = new com.visor.school.academicservice.model.Class("Mathematics 101", ClassType.SUBJECT, "Mathematics", 10, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        entityManager.persistAndFlush(testSubjectClass);
    }

    @Test
    void shouldFindClassByClassName() {
        List<com.visor.school.academicservice.model.Class> found = classRepository.findByClassName("Grade 3 Homeroom");
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(c -> "Grade 3 Homeroom".equals(c.getClassName())));
    }

    @Test
    void shouldFindClassesByGradeLevel() {
        List<com.visor.school.academicservice.model.Class> grade3Classes = classRepository.findByGradeLevel(3);
        List<com.visor.school.academicservice.model.Class> grade10Classes = classRepository.findByGradeLevel(10);

        assertFalse(grade3Classes.isEmpty());
        assertTrue(grade3Classes.stream().allMatch(c -> c.getGradeLevel() == 3));
        assertFalse(grade10Classes.isEmpty());
        assertTrue(grade10Classes.stream().allMatch(c -> c.getGradeLevel() == 10));
    }

    @Test
    void shouldFindClassesByClassType() {
        List<com.visor.school.academicservice.model.Class> homeroomClasses = classRepository.findByClassType(ClassType.HOMEROOM);
        List<com.visor.school.academicservice.model.Class> subjectClasses = classRepository.findByClassType(ClassType.SUBJECT);

        assertFalse(homeroomClasses.isEmpty());
        assertTrue(homeroomClasses.stream().allMatch(c -> c.getClassType() == ClassType.HOMEROOM));
        assertFalse(subjectClasses.isEmpty());
        assertTrue(subjectClasses.stream().allMatch(c -> c.getClassType() == ClassType.SUBJECT));
    }

    @Test
    void shouldFindClassesByAcademicYearAndTerm() {
        List<com.visor.school.academicservice.model.Class> classes = classRepository.findByAcademicYearAndTerm("2024-2025", Term.FIRST_TERM);
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().allMatch(c -> "2024-2025".equals(c.getAcademicYear()) && c.getTerm() == Term.FIRST_TERM));
    }

    @Test
    void shouldFindHomeroomClassesByTeacher() {
        UUID homeroomTeacherId = testHomeroomClass.getHomeroomTeacherId();
        List<com.visor.school.academicservice.model.Class> classes = classRepository.findByHomeroomTeacherId(homeroomTeacherId);
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().allMatch(c -> homeroomTeacherId.equals(c.getHomeroomTeacherId())));
    }

    @Test
    void shouldFindSubjectClassesByClassTeacher() {
        UUID classTeacherId = UUID.randomUUID();
        com.visor.school.academicservice.model.Class classWithTeacher = new com.visor.school.academicservice.model.Class("English 101", ClassType.SUBJECT, "English", 11, null, classTeacherId, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.SCHEDULED);
        entityManager.persistAndFlush(classWithTeacher);

        List<com.visor.school.academicservice.model.Class> classes = classRepository.findByClassTeacherId(classTeacherId);
        assertFalse(classes.isEmpty());
        assertTrue(classes.stream().allMatch(c -> classTeacherId.equals(c.getClassTeacherId())));
    }

    @Test
    void shouldFindClassesByStatus() {
        com.visor.school.academicservice.model.Class inProgressClass = new com.visor.school.academicservice.model.Class("In Progress Class", ClassType.SUBJECT, "Science", 8, null, null, "2024-2025", Term.FIRST_TERM, null, null, LocalDate.of(2024, 9, 1), null, ClassStatus.IN_PROGRESS);
        entityManager.persistAndFlush(inProgressClass);

        List<com.visor.school.academicservice.model.Class> inProgressClasses = classRepository.findByStatus(ClassStatus.IN_PROGRESS);
        assertFalse(inProgressClasses.isEmpty());
        assertTrue(inProgressClasses.stream().allMatch(c -> c.getStatus() == ClassStatus.IN_PROGRESS));
    }
}
