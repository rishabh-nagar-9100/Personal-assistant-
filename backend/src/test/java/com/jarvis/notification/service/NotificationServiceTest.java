package com.jarvis.notification.service;

import com.jarvis.auth.model.User;
import com.jarvis.notification.dto.NotificationResponse;
import com.jarvis.notification.dto.RegisterTokenRequest;
import com.jarvis.notification.model.Notification;
import com.jarvis.notification.model.NotificationToken;
import com.jarvis.notification.model.NotificationType;
import com.jarvis.notification.repository.NotificationRepository;
import com.jarvis.notification.repository.NotificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationTokenRepository tokenRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;
    private User testUser;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(tokenRepository, notificationRepository);
        testUser = new User(UUID.randomUUID(), "notifuser@example.com");
    }

    @Test
    @DisplayName("Should register new FCM token when token does not exist")
    void testRegisterNewFcmToken() {
        when(tokenRepository.findByUserIdAndFcmToken(testUser.getId(), "token123")).thenReturn(Optional.empty());

        notificationService.registerFcmToken(testUser, new RegisterTokenRequest("token123", "FLUTTER_ANDROID"));

        verify(tokenRepository, times(1)).save(any(NotificationToken.class));
    }

    @Test
    @DisplayName("Should update device type when FCM token already exists")
    void testUpdateExistingFcmToken() {
        NotificationToken existing = new NotificationToken(testUser, "token123", "FLUTTER_ANDROID");
        when(tokenRepository.findByUserIdAndFcmToken(testUser.getId(), "token123")).thenReturn(Optional.of(existing));

        notificationService.registerFcmToken(testUser, new RegisterTokenRequest("token123", "FLUTTER_IOS"));

        assertEquals("FLUTTER_IOS", existing.getDeviceType());
        verify(tokenRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Should fetch pending unread notifications scheduled up to current time")
    void testGetPendingNotifications() {
        Notification n1 = new Notification(testUser, NotificationType.MORNING_BRIEFING, "Briefing", "Good morning", Instant.now());
        when(notificationRepository.findByUserIdAndIsReadFalseAndScheduledForLessThanEqualOrderByScheduledForDesc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(n1));

        List<NotificationResponse> pending = notificationService.getPendingNotifications(testUser);

        assertEquals(1, pending.size());
        assertEquals("Briefing", pending.get(0).title());
        assertFalse(pending.get(0).isRead());
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead() {
        UUID notifId = UUID.randomUUID();
        Notification notification = new Notification(testUser, NotificationType.EVENING_REVISION, "Revision", "Time to revise", Instant.now());

        when(notificationRepository.findByIdAndUserId(notifId, testUser.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(testUser, notifId);

        assertTrue(response.isRead());
    }
}
