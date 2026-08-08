package com.jarvis.notification.dto;

import com.jarvis.notification.model.Notification;
import com.jarvis.notification.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String body,
        boolean isRead,
        Instant scheduledFor,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getScheduledFor(),
                notification.getCreatedAt()
        );
    }
}
