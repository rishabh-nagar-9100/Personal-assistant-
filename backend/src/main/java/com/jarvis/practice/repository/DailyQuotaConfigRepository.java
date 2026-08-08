package com.jarvis.practice.repository;

import com.jarvis.practice.model.DailyQuotaConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DailyQuotaConfigRepository extends JpaRepository<DailyQuotaConfig, UUID> {
}
