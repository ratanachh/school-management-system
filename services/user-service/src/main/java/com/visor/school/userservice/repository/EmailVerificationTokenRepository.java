package com.visor.school.userservice.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visor.school.userservice.model.EmailVerificationToken;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(UUID userId);

    void deleteByExpiresAtBefore(Instant instant);
}
