package com.jarvis.notification.service;

import com.jarvis.auth.model.User;
import com.jarvis.notification.dto.NotificationResponse;
import com.jarvis.notification.dto.RegisterTokenRequest;
import com.jarvis.notification.model.Notification;
import com.jarvis.notification.model.NotificationToken;
import com.jarvis.notification.model.NotificationType;
import com.jarvis.notification.repository.NotificationRepository;
import com.jarvis.notification.repository.NotificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationTokenRepository tokenRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationTokenRepository tokenRepository,
                               NotificationRepository notificationRepository) {
        this.tokenRepository = tokenRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void registerFcmToken(User user, RegisterTokenRequest request) {
        Optional<NotificationToken> existing = tokenRepository.findByUserIdAndFcmToken(user.getId(), request.fcmToken());
        if (existing.isPresent()) {
            NotificationToken token = existing.get();
            token.setDeviceType(request.deviceType());
            tokenRepository.save(token);
        } else {
            NotificationToken newToken = new NotificationToken(user, request.fcmToken(), request.deviceType());
            tokenRepository.save(newToken);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getPendingNotifications(User user) {
        return notificationRepository.findByUserIdAndIsReadFalseAndScheduledForLessThanEqualOrderByScheduledForDesc(user.getId(), Instant.now())
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications(User user) {
        return notificationRepository.findByUserIdOrderByScheduledForDesc(user.getId())
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(User user, UUID id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @Transactional
    public NotificationResponse createNotification(User user, NotificationType type, String title, String body, Instant scheduledFor) {
        Notification notification = new Notification(user, type, title, body, scheduledFor);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }
}
