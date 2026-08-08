package com.jarvis.timetable.dto;

import java.time.LocalDate;
import java.util.List;

public record DayOrderInfoResponse(
        String dayOrder,
        LocalDate date,
        boolean isConfigured,
        List<SlotResponse> classSlots,
        List<FreeSlotResponse> freeSlots
) {
}
