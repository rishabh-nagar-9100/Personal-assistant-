package com.jarvis.topic.dto;

import com.jarvis.topic.model.Topic;
import com.jarvis.topic.model.TopicStatus;

import java.time.Instant;
import java.util.UUID;

public record TopicResponse(
        UUID id,
        UUID subjectId,
        String subjectName,
        String name,
        TopicStatus status,
        Instant lastStudiedAt,
        Instant nextRevisionAt,
        double easeFactor,
        int intervalDays,
        int repetitionCount
) {
    public static TopicResponse fromEntity(Topic topic) {
        return new TopicResponse(
                topic.getId(),
                topic.getSubject().getId(),
                topic.getSubject().getName(),
                topic.getName(),
                topic.getStatus(),
                topic.getLastStudiedAt(),
                topic.getNextRevisionAt(),
                topic.getEaseFactor(),
                topic.getIntervalDays(),
                topic.getRepetitionCount()
        );
    }
}
