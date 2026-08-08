package com.jarvis.practice.dto;

import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.model.PracticeQuestion;

import java.time.Instant;
import java.util.UUID;

public record PracticeQuestionResponse(
        UUID id,
        PracticeCategoryType categoryType,
        String subCategory,
        String title,
        DsaDifficulty difficulty,
        DsaStatus status,
        Instant lastAttemptedAt,
        Instant nextRevisionAt,
        double easeFactor,
        int intervalDays,
        int repetitionCount
) {
    public static PracticeQuestionResponse fromEntity(PracticeQuestion question) {
        return new PracticeQuestionResponse(
                question.getId(),
                question.getCategoryType(),
                question.getSubCategory(),
                question.getTitle(),
                question.getDifficulty(),
                question.getStatus(),
                question.getLastAttemptedAt(),
                question.getNextRevisionAt(),
                question.getEaseFactor(),
                question.getIntervalDays(),
                question.getRepetitionCount()
        );
    }
}
