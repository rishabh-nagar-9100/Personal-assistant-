package com.jarvis.spacedrepetition.service;

import com.jarvis.spacedrepetition.dto.SpacedRepetitionResult;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SpacedRepetitionCalculator {

    private static final double MIN_EASE_FACTOR = 1.3;

    /**
     * Calculates SM-2 parameters for a topic or practice question based on review feedback.
     *
     * Rules (ARCHITECTURE.md §6):
     * - GOOD (quality >= 3): interval = Math.round(interval * ease_factor), ease_factor += 0.1
     * - STRUGGLED (quality < 3): interval = 1, ease_factor -= 0.2 (min 1.3)
     * - next_revision_at = now + interval_days
     */
    public SpacedRepetitionResult calculateNextRevision(double currentEaseFactor,
                                                         int currentIntervalDays,
                                                         int currentRepetitionCount,
                                                         ReviewQuality quality) {
        double newEaseFactor;
        int newIntervalDays;
        int newRepetitionCount;

        if (quality == ReviewQuality.GOOD) {
            newEaseFactor = currentEaseFactor + 0.1;
            if (currentRepetitionCount == 0) {
                newIntervalDays = 1;
            } else if (currentRepetitionCount == 1) {
                newIntervalDays = 3;
            } else {
                newIntervalDays = (int) Math.round(currentIntervalDays * currentEaseFactor);
            }
            newRepetitionCount = currentRepetitionCount + 1;
        } else {
            newEaseFactor = Math.max(MIN_EASE_FACTOR, currentEaseFactor - 0.2);
            newIntervalDays = 1;
            newRepetitionCount = 0;
        }

        Instant now = Instant.now();
        Instant nextRevisionAt = now.plus(newIntervalDays, ChronoUnit.DAYS);

        return new SpacedRepetitionResult(
                Math.round(newEaseFactor * 100.0) / 100.0,
                newIntervalDays,
                newRepetitionCount,
                now,
                nextRevisionAt
        );
    }
}
