package com.jarvis.task.dto;

import com.jarvis.task.model.Task;
import com.jarvis.task.model.TaskPriority;
import com.jarvis.task.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Instant dueDate,
        TaskPriority priority,
        UUID linkedTopicId,
        TaskStatus status
) {
    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.getLinkedTopicId(),
                task.getStatus()
        );
    }
}
