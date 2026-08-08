package com.jarvis.briefing.model;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_briefings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date"})
})
public class DailyBriefing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "briefing_text", nullable = false, columnDefinition = "TEXT")
    private String briefingText;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected DailyBriefing() {
        // JPA requires no-arg constructor
    }

    public DailyBriefing(User user, LocalDate date, String briefingText) {
        this.user = user;
        this.date = date;
        this.briefingText = briefingText;
        this.generatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getBriefingText() {
        return briefingText;
    }

    public void setBriefingText(String briefingText) {
        this.briefingText = briefingText;
        this.generatedAt = Instant.now();
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
