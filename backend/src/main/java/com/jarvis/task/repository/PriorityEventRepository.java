package com.jarvis.task.repository;

import com.jarvis.task.model.PriorityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriorityEventRepository extends JpaRepository<PriorityEvent, UUID> {

    List<PriorityEvent> findByUserIdOrderByEventDateAsc(UUID userId);

    List<PriorityEvent> findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(UUID userId, Instant fromDate);

    Optional<PriorityEvent> findByIdAndUserId(UUID id, UUID userId);
}
