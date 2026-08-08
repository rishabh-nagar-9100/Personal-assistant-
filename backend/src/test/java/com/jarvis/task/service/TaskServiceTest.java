package com.jarvis.task.service;

import com.jarvis.auth.model.User;
import com.jarvis.task.dto.CreateTaskRequest;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.dto.UpdateTaskStatusRequest;
import com.jarvis.task.model.Task;
import com.jarvis.task.model.TaskPriority;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskService taskService;
    private User testUser;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
        testUser = new User(UUID.randomUUID(), "taskuser@example.com");
    }

    @Test
    @DisplayName("Should create task successfully with PENDING status")
    void testCreateTask() {
        Instant dueDate = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateTaskRequest request = new CreateTaskRequest(
                "Submit Resume",
                "Update resume with recent projects",
                dueDate,
                TaskPriority.HIGH,
                null
        );

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(testUser, request);

        assertNotNull(response);
        assertEquals("Submit Resume", response.title());
        assertEquals(TaskPriority.HIGH, response.priority());
        assertEquals(TaskStatus.PENDING, response.status());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task status to DONE")
    void testUpdateTaskStatus() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(testUser, "Study SQL", "Practice subqueries", Instant.now(), TaskPriority.MEDIUM, null);

        when(taskRepository.findByIdAndUserId(taskId, testUser.getId())).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTaskStatus(testUser, taskId, new UpdateTaskStatusRequest(TaskStatus.DONE));

        assertEquals(TaskStatus.DONE, response.status());
        verify(taskRepository, times(1)).save(existingTask);
    }
}
