package com.visor.school.userservice.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.model.Parent;
import com.visor.school.userservice.model.Relationship;
import com.visor.school.userservice.repository.ParentRepository;

/**
 * Parent service for managing parent-student relationships
 */
@Service
@Transactional
public class ParentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ParentRepository parentRepository;

    public ParentService(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    /**
     * Link a student to a parent
     */
    public Parent linkStudentToParent(
        UUID userId,
        UUID studentId,
        Relationship relationship,
        boolean isPrimary
    ) {
        logger.info("Linking student {} to parent {} with relationship: {}", studentId, userId, relationship);

        if (parentRepository.existsByUserIdAndStudentId(userId, studentId)) {
            throw new IllegalArgumentException("Parent-student relationship already exists");
        }

        Parent parent = new Parent(
            userId,
            studentId,
            relationship,
            isPrimary
        );

        Parent saved = parentRepository.save(parent);
        logger.info("Parent-student relationship created: {}", saved.getId());

        return saved;
    }

    /**
     * Get all children (students) for a parent
     */
    @Transactional(readOnly = true)
    public List<Parent> getChildrenByParent(UUID userId) {
        return parentRepository.findByUserId(userId);
    }

    /**
     * Get all parents for a student
     */
    @Transactional(readOnly = true)
    public List<Parent> getParentsByStudent(UUID studentId) {
        return parentRepository.findByStudentId(studentId);
    }

    /**
     * Remove parent-student relationship
     */
    public void unlinkStudentFromParent(UUID userId, UUID studentId) {
        logger.info("Unlinking student {} from parent {}", studentId, userId);

        List<Parent> parents = parentRepository.findByUserId(userId);
        Parent parent = parents.stream()
            .filter(p -> p.getStudentId().equals(studentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Parent-student relationship not found"));

        parentRepository.delete(parent);
        logger.info("Parent-student relationship removed");
    }
}
