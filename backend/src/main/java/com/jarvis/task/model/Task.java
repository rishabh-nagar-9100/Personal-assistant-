package com.jarvis.task.model;

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
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskPriority priority;

    @Column(name = "linked_topic_id")
    private UUID linkedTopicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskStatus status;

    protected Task() {
        // JPA requires no-arg constructor
    }

    public Task(User user, String title, String description, Instant dueDate, TaskPriority priority, UUID linkedTopicId) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.linkedTopicId = linkedTopicId;
        this.status = TaskStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public UUID getLinkedTopicId() {
        return linkedTopicId;
    }

    public void setLinkedTopicId(UUID linkedTopicId) {
        this.linkedTopicId = linkedTopicId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
