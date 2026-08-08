package com.jarvis.topic.dto;

import java.util.UUID;

public record CreateTopicRequest(
        UUID subjectId,
        String name
) {
}
