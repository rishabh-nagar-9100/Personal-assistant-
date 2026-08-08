package com.jarvis.task.repository;

import com.jarvis.task.model.Task;
import com.jarvis.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserIdOrderByDueDateAsc(UUID userId);

    List<Task> findByUserIdAndStatusOrderByDueDateAsc(UUID userId, TaskStatus status);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);
}
