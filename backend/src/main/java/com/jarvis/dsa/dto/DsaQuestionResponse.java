package com.jarvis.dsa.dto;

import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;

import java.time.Instant;
import java.util.UUID;

public record DsaQuestionResponse(
        UUID id,
        String title,
        String topic,
        DsaDifficulty difficulty,
        String sourceLink,
        DsaStatus status,
        Instant lastAttemptedAt,
        Instant nextRevisionAt,
        double easeFactor,
        int intervalDays,
        int repetitionCount
) {
    public static DsaQuestionResponse fromEntity(DsaQuestion question) {
        return new DsaQuestionResponse(
                question.getId(),
                question.getTitle(),
                question.getTopic(),
                question.getDifficulty(),
                question.getSourceLink(),
                question.getStatus(),
                question.getLastAttemptedAt(),
                question.getNextRevisionAt(),
                question.getEaseFactor(),
                question.getIntervalDays(),
                question.getRepetitionCount()
        );
    }
}
