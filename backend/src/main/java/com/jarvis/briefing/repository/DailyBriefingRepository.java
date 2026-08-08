package com.jarvis.briefing.repository;

import com.jarvis.briefing.model.DailyBriefing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyBriefingRepository extends JpaRepository<DailyBriefing, UUID> {

    Optional<DailyBriefing> findByUserIdAndDate(UUID userId, LocalDate date);
}
