package com.jarvis.notification.dto;

public record RegisterTokenRequest(
        String fcmToken,
        String deviceType
) {
}
