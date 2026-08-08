package com.jarvis.notification.model;

import com.jarvis.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "scheduled_for", nullable = false)
    private Instant scheduledFor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
        // JPA requires no-arg constructor
    }

    public Notification(User user, NotificationType type, String title, String body, Instant scheduledFor) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.scheduledFor = scheduledFor;
        this.createdAt = Instant.now();
        this.isRead = false;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getScheduledFor() {
        return scheduledFor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
