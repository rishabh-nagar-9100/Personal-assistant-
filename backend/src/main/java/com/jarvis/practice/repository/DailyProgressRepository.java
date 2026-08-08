package com.jarvis.practice.repository;

import com.jarvis.practice.model.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, UUID> {

    Optional<DailyProgress> findByUserIdAndDate(UUID userId, LocalDate date);
}
