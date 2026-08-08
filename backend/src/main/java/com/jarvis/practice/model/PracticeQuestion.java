package com.jarvis.practice.model;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaStatus;
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
@Table(name = "practice_questions")
public class PracticeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private com.jarvis.topic.model.Subject subject;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "problem_number", length = 50)
    private String problemNumber;

    @Column(name = "source_link", columnDefinition = "TEXT")
    private String sourceLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 20)
    private PracticeCategoryType categoryType;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DsaDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DsaStatus status;

    @Column(name = "last_attempted_at")
    private Instant lastAttemptedAt;

    @Column(name = "next_revision_at")
    private Instant nextRevisionAt;

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor = 2.5;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays = 1;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount = 0;

    protected PracticeQuestion() {
        // JPA requires no-arg constructor
    }

    public PracticeQuestion(User user, PracticeCategoryType categoryType, String subCategory, String title, DsaDifficulty difficulty) {
        this.user = user;
        this.categoryType = categoryType;
        this.subCategory = subCategory;
        this.title = title;
        this.difficulty = difficulty != null ? difficulty : DsaDifficulty.MEDIUM;
        this.status = DsaStatus.NOT_STARTED;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public PracticeCategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(PracticeCategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DsaDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DsaDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public DsaStatus getStatus() {
        return status;
    }

    public void setStatus(DsaStatus status) {
        this.status = status;
    }

    public Instant getLastAttemptedAt() {
        return lastAttemptedAt;
    }

    public void setLastAttemptedAt(Instant lastAttemptedAt) {
        this.lastAttemptedAt = lastAttemptedAt;
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

    public com.jarvis.topic.model.Subject getSubject() {
        return subject;
    }

    public void setSubject(com.jarvis.topic.model.Subject subject) {
        this.subject = subject;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getProblemNumber() {
        return problemNumber;
    }

    public void setProblemNumber(String problemNumber) {
        this.problemNumber = problemNumber;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(int repetitionCount) {
        this.repetitionCount = repetitionCount;
    }
}
