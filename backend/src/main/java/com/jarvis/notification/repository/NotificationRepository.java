package com.jarvis.notification.repository;

import com.jarvis.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdAndIsReadFalseAndScheduledForLessThanEqualOrderByScheduledForDesc(UUID userId, Instant scheduledFor);

    List<Notification> findByUserIdOrderByScheduledForDesc(UUID userId);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
