package com.jarvis.timetable.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record FreeSlotResponse(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        long durationMinutes
) {
}
