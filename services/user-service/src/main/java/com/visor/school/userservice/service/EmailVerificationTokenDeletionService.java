package com.visor.school.userservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.visor.school.userservice.repository.EmailVerificationTokenRepository;

/**
 * Deletes verification token rows in a separate transaction so removals persist
 * even when the caller throws (e.g. invalid/expired token) and rolls back its own transaction.
 */
@Service
public class EmailVerificationTokenDeletionService {

    private final EmailVerificationTokenRepository repository;

    public EmailVerificationTokenDeletionService(EmailVerificationTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteByToken(String token) {
        repository.deleteByToken(token);
    }
}
