package com.jarvis.scheduler.dto;

import com.jarvis.timetable.dto.FreeSlotResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record DailyScheduleResponse(
        LocalDate date,
        DayOfWeek dayOfWeek,
        List<FreeSlotResponse> freeTimeBlocks,
        List<ScheduledSlotItem> scheduledItems,
        List<OverflowItem> overflowItems,
        boolean hasPriorityEvents,
        String summaryText
) {
}
