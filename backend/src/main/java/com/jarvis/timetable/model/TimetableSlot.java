package com.jarvis.timetable.model;

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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "timetable_slots")
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "day_order", length = 20)
    private String dayOrder;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SlotType type;

    @Column(nullable = false)
    private String label;

    protected TimetableSlot() {
        // JPA requires no-arg constructor
    }

    public TimetableSlot(User user, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, SlotType type, String label) {
        this(user, dayOfWeek, null, startTime, endTime, type, label);
    }

    public TimetableSlot(User user, DayOfWeek dayOfWeek, String dayOrder, LocalTime startTime, LocalTime endTime, SlotType type, String label) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("endTime must be strictly after startTime");
        }
        this.user = user;
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : DayOfWeek.MONDAY;
        this.dayOrder = dayOrder;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.label = label;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(String dayOrder) {
        this.dayOrder = dayOrder;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public SlotType getType() {
        return type;
    }

    public void setType(SlotType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
