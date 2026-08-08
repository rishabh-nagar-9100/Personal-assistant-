package com.jarvis.topic.dto;

import com.jarvis.topic.model.Subject;

import java.util.UUID;

public record SubjectResponse(
        UUID id,
        String name
) {
    public static SubjectResponse fromEntity(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName());
    }
}
