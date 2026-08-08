package com.jarvis.task.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.task.dto.CreateTaskRequest;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.dto.UpdateTaskStatusRequest;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(JwtAuthenticationToken authToken,
                                                   @RequestBody CreateTaskRequest request) {
        User user = userService.getOrCreateUser(authToken);
        TaskResponse response = taskService.createTask(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(JwtAuthenticationToken authToken,
                                                       @RequestParam(required = false) TaskStatus status) {
        User user = userService.getOrCreateUser(authToken);
        List<TaskResponse> responses = taskService.getUserTasks(user, status);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(JwtAuthenticationToken authToken,
                                                         @PathVariable UUID id,
                                                         @RequestBody UpdateTaskStatusRequest request) {
        User user = userService.getOrCreateUser(authToken);
        TaskResponse response = taskService.updateTaskStatus(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(JwtAuthenticationToken authToken,
                                           @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        taskService.deleteTask(user, id);
        return ResponseEntity.noContent().build();
    }
}
