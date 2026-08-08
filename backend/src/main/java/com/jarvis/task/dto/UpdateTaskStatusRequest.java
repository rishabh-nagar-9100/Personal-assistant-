package com.jarvis.task.dto;

import com.jarvis.task.model.TaskStatus;

public record UpdateTaskStatusRequest(
        TaskStatus status
) {
}
