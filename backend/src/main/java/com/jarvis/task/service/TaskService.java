package com.jarvis.task.service;

import com.jarvis.auth.model.User;
import com.jarvis.task.dto.CreateTaskRequest;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.dto.UpdateTaskStatusRequest;
import com.jarvis.task.model.Task;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponse createTask(User user, CreateTaskRequest request) {
        Task task = new Task(
                user,
                request.title(),
                request.description(),
                request.dueDate(),
                request.priority(),
                request.linkedTopicId()
        );
        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getUserTasks(User user, TaskStatus status) {
        List<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByUserIdAndStatusOrderByDueDateAsc(user.getId(), status);
        } else {
            tasks = taskRepository.findByUserIdOrderByDueDateAsc(user.getId());
        }
        return tasks.stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional
    public TaskResponse updateTaskStatus(User user, UUID taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        task.setStatus(request.status());
        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTask(User user, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        taskRepository.delete(task);
    }
}
