package com.jarvis.task.dto;

import com.jarvis.task.model.PriorityEventType;

import java.time.Instant;
import java.util.List;

public record CreatePriorityEventRequest(
        String name,
        Instant eventDate,
        PriorityEventType type,
        String jdText,
        List<String> boostedTopicIds
) {
}
