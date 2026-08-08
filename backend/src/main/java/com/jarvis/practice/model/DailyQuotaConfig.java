package com.jarvis.practice.model;

import com.jarvis.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;

import java.util.UUID;

@Entity
@Table(name = "daily_quota_config")
public class DailyQuotaConfig implements Persistable<UUID> {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "dsa_target", nullable = false)
    private int dsaTarget = 5;

    @Column(name = "sql_target", nullable = false)
    private int sqlTarget = 5;

    @Column(name = "aptitude_target", nullable = false)
    private int aptitudeTarget = 5;

    @Transient
    private boolean isNew = true;

    protected DailyQuotaConfig() {
        // JPA requires no-arg constructor
    }

    public DailyQuotaConfig(User user, int dsaTarget, int sqlTarget, int aptitudeTarget) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.dsaTarget = dsaTarget;
        this.sqlTarget = sqlTarget;
        this.aptitudeTarget = aptitudeTarget;
        this.isNew = true;
    }

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    public UUID getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public int getDsaTarget() {
        return dsaTarget;
    }

    public void setDsaTarget(int dsaTarget) {
        this.dsaTarget = dsaTarget;
    }

    public int getSqlTarget() {
        return sqlTarget;
    }

    public void setSqlTarget(int sqlTarget) {
        this.sqlTarget = sqlTarget;
    }

    public int getAptitudeTarget() {
        return aptitudeTarget;
    }

    public void setAptitudeTarget(int aptitudeTarget) {
        this.aptitudeTarget = aptitudeTarget;
    }
}
