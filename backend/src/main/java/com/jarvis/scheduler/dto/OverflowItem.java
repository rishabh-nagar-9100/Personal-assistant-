package com.jarvis.scheduler.dto;

import com.jarvis.scheduler.model.ScheduledItemType;

import java.time.LocalDate;
import java.util.UUID;

public record OverflowItem(
        ScheduledItemType itemType,
        String title,
        String reason,
        UUID referenceId,
        LocalDate suggestedCarryOverDate
) {
}
