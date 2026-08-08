package com.jarvis.timetable.dto;

import java.util.List;

public record BulkSlotRequest(
        List<CreateSlotRequest> slots,
        boolean replaceExisting
) {
}
