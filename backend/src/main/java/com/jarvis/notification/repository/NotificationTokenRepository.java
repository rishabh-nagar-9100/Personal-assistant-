package com.jarvis.notification.repository;

import com.jarvis.notification.model.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, UUID> {

    Optional<NotificationToken> findByUserIdAndFcmToken(UUID userId, String fcmToken);

    List<NotificationToken> findByUserId(UUID userId);
}
