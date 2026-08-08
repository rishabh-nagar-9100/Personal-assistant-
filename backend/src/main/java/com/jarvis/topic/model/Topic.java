package com.jarvis.topic.model;

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
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopicStatus status;

    @Column(name = "last_studied_at")
    private Instant lastStudiedAt;

    @Column(name = "next_revision_at")
    private Instant nextRevisionAt;

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays = 1;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount = 0;

    protected Topic() {
        // JPA requires no-arg constructor
    }

    public Topic(Subject subject, String name) {
        this.subject = subject;
        this.name = name;
        this.status = TopicStatus.NOT_STARTED;
    }

    public UUID getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public Instant getLastStudiedAt() {
        return lastStudiedAt;
    }

    public void setLastStudiedAt(Instant lastStudiedAt) {
        this.lastStudiedAt = lastStudiedAt;
    }

    public Instant getNextRevisionAt() {
        return nextRevisionAt;
    }

    public void setNextRevisionAt(Instant nextRevisionAt) {
        this.nextRevisionAt = nextRevisionAt;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(double easeFactor) {
        this.easeFactor = easeFactor;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
    }

    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(int repetitionCount) {
        this.repetitionCount = repetitionCount;
    }
}
