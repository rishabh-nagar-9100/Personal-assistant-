package com.jarvis.topic.dto;

import java.util.UUID;

public record SubjectSummaryResponse(
        UUID id,
        String name,
        int topicCount,
        int totalQuestions,
        int solvedQuestions,
        int inProgressQuestions,
        int needsRevisionQuestions,
        int notStartedQuestions
) {}
