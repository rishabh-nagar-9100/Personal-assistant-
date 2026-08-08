package com.jarvis.notification.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.notification.dto.NotificationResponse;
import com.jarvis.notification.dto.RegisterTokenRequest;
import com.jarvis.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(JwtAuthenticationToken authToken,
                                                @RequestBody RegisterTokenRequest request) {
        User user = userService.getOrCreateUser(authToken);
        notificationService.registerFcmToken(user, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<NotificationResponse> pending = notificationService.getPendingNotifications(user);
        return ResponseEntity.ok(pending);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<NotificationResponse> notifications = notificationService.getAllNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(JwtAuthenticationToken authToken,
                                                           @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        NotificationResponse updated = notificationService.markAsRead(user, id);
        return ResponseEntity.ok(updated);
    }
}
