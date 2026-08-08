package com.jarvis.spacedrepetition.dto;

import java.time.Instant;

public record SpacedRepetitionResult(
        double easeFactor,
        int intervalDays,
        int repetitionCount,
        Instant lastStudiedAt,
        Instant nextRevisionAt
) {
}
