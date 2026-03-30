package com.visor.school.userservice.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.visor.school.userservice.model.OutboxEvent;
import com.visor.school.userservice.model.OutboxStatus;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
        OutboxStatus status,
        Instant nextAttemptAt
    );
}
