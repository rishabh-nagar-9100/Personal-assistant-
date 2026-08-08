package com.jarvis.practice.dto;

import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaStatus;

import java.time.Instant;
import java.util.UUID;

public record SubjectQuestionResponse(
        UUID id,
        UUID subjectId,
        String subjectName,
        String topicName,
        String title,
        String problemNumber,
        DsaDifficulty difficulty,
        DsaStatus status,
        String sourceLink,
        Instant lastAttemptedAt,
        Instant nextRevisionAt,
        double easeFactor,
        int repetitionCount
) {}
