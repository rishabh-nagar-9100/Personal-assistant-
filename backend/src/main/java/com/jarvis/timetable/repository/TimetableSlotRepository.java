package com.jarvis.timetable.repository;

import com.jarvis.timetable.model.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    List<TimetableSlot> findByUserIdAndDayOfWeekOrderByStartTimeAsc(UUID userId, DayOfWeek dayOfWeek);

    List<TimetableSlot> findByUserIdAndDayOrderOrderByStartTimeAsc(UUID userId, String dayOrder);

    List<TimetableSlot> findByUserIdOrderByDayOfWeekAscStartTimeAsc(UUID userId);

    List<TimetableSlot> findByUserIdOrderByDayOrderAscStartTimeAsc(UUID userId);

    Optional<TimetableSlot> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);
}
