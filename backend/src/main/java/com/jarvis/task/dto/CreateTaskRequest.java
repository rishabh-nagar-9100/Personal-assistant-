package com.jarvis.task.dto;

import com.jarvis.task.model.TaskPriority;

import java.time.Instant;
import java.util.UUID;

public record CreateTaskRequest(
        String title,
        String description,
        Instant dueDate,
        TaskPriority priority,
        UUID linkedTopicId
) {
}
