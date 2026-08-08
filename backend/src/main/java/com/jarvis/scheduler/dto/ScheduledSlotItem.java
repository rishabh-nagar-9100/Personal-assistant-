package com.jarvis.scheduler.dto;

import com.jarvis.scheduler.model.ScheduledItemType;

import java.time.LocalTime;
import java.util.UUID;

public record ScheduledSlotItem(
        LocalTime startTime,
        LocalTime endTime,
        long durationMinutes,
        ScheduledItemType itemType,
        String title,
        String details,
        UUID referenceId
) {
}
