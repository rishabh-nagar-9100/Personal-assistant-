package com.jarvis.task.dto;

import com.jarvis.task.model.PriorityEvent;
import com.jarvis.task.model.PriorityEventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PriorityEventResponse(
        UUID id,
        String name,
        Instant eventDate,
        PriorityEventType type,
        String jdText,
        List<String> boostedTopicIds
) {
    public static PriorityEventResponse fromEntity(PriorityEvent event) {
        return new PriorityEventResponse(
                event.getId(),
                event.getName(),
                event.getEventDate(),
                event.getType(),
                event.getJdText(),
                event.getBoostedTopicIds()
        );
    }
}
