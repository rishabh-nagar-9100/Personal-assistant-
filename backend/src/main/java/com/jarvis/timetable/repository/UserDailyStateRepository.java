package com.jarvis.timetable.repository;

import com.jarvis.timetable.model.UserDailyState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface UserDailyStateRepository extends JpaRepository<UserDailyState, UUID> {

    Optional<UserDailyState> findByUserIdAndDate(UUID userId, LocalDate date);

    void deleteByUserId(UUID userId);
}
