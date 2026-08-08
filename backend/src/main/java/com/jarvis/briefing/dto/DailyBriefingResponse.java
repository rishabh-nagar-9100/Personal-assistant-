package com.jarvis.briefing.dto;

import java.time.Instant;
import java.time.LocalDate;

public record DailyBriefingResponse(
        LocalDate date,
        String briefingText,
        boolean isCached,
        Instant generatedAt
) {
}
