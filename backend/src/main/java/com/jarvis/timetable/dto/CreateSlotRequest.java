package com.jarvis.timetable.dto;

import com.jarvis.timetable.model.SlotType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateSlotRequest(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        SlotType type,
        String label
) {
}
