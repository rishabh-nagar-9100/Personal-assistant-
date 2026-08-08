package com.jarvis.notification.model;

import com.jarvis.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "fcm_token"})
})
public class NotificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, columnDefinition = "TEXT")
    private String fcmToken;

    @Column(name = "device_type", length = 20)
    private String deviceType = "FLUTTER";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationToken() {
        // JPA requires no-arg constructor
    }

    public NotificationToken(User user, String fcmToken, String deviceType) {
        this.user = user;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType != null ? deviceType : "FLUTTER";
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.updatedAt = Instant.now();
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
