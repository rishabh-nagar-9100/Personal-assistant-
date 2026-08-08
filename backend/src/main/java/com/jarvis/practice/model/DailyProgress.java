package com.jarvis.practice.model;

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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date"})
})
public class DailyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "dsa_done", nullable = false)
    private int dsaDone = 0;

    @Column(name = "sql_done", nullable = false)
    private int sqlDone = 0;

    @Column(name = "aptitude_done", nullable = false)
    private int aptitudeDone = 0;

    protected DailyProgress() {
        // JPA requires no-arg constructor
    }

    public DailyProgress(User user, LocalDate date) {
        this.user = user;
        this.date = date;
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

    public int getDsaDone() {
        return dsaDone;
    }

    public void setDsaDone(int dsaDone) {
        this.dsaDone = dsaDone;
    }

    public int getSqlDone() {
        return sqlDone;
    }

    public void setSqlDone(int sqlDone) {
        this.sqlDone = sqlDone;
    }

    public int getAptitudeDone() {
        return aptitudeDone;
    }

    public void setAptitudeDone(int aptitudeDone) {
        this.aptitudeDone = aptitudeDone;
    }
}
