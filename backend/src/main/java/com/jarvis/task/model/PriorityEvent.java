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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "priority_events")
public class PriorityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PriorityEventType type;

    @Column(name = "jd_text", columnDefinition = "TEXT")
    private String jdText;

    @Column(name = "boosted_topic_ids")
    private List<String> boostedTopicIds = new ArrayList<>();

    protected PriorityEvent() {
        // JPA requires no-arg constructor
    }

    public PriorityEvent(User user, String name, Instant eventDate, PriorityEventType type, String jdText, List<String> boostedTopicIds) {
        this.user = user;
        this.name = name;
        this.eventDate = eventDate;
        this.type = type;
        this.jdText = jdText;
        if (boostedTopicIds != null) {
            this.boostedTopicIds = boostedTopicIds;
        }
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getEventDate() {
        return eventDate;
    }

    public void setEventDate(Instant eventDate) {
        this.eventDate = eventDate;
    }

    public PriorityEventType getType() {
        return type;
    }

    public void setType(PriorityEventType type) {
        this.type = type;
    }

    public String getJdText() {
        return jdText;
    }

    public void setJdText(String jdText) {
        this.jdText = jdText;
    }

    public List<String> getBoostedTopicIds() {
        return boostedTopicIds;
    }

    public void setBoostedTopicIds(List<String> boostedTopicIds) {
        this.boostedTopicIds = boostedTopicIds;
    }
}
