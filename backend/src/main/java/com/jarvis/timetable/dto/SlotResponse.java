package com.jarvis.timetable.dto;

import com.jarvis.timetable.model.SlotType;
import com.jarvis.timetable.model.TimetableSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record SlotResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        String dayOrder,
        LocalTime startTime,
        LocalTime endTime,
        SlotType type,
        String label
) {
    public static SlotResponse fromEntity(TimetableSlot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getDayOfWeek(),
                slot.getDayOrder(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getType(),
                slot.getLabel()
        );
    }
}
