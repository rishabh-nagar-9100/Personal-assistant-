package com.jarvis.task.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.task.dto.CreatePriorityEventRequest;
import com.jarvis.task.dto.PriorityEventResponse;
import com.jarvis.task.service.PriorityEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/priority-events")
public class PriorityEventController {

    private final PriorityEventService priorityEventService;
    private final UserService userService;

    public PriorityEventController(PriorityEventService priorityEventService, UserService userService) {
        this.priorityEventService = priorityEventService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<PriorityEventResponse> createEvent(JwtAuthenticationToken authToken,
                                                             @RequestBody CreatePriorityEventRequest request) {
        User user = userService.getOrCreateUser(authToken);
        PriorityEventResponse response = priorityEventService.createEvent(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PriorityEventResponse>> getAllEvents(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<PriorityEventResponse> responses = priorityEventService.getAllEvents(user);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<PriorityEventResponse>> getUpcomingEvents(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<PriorityEventResponse> responses = priorityEventService.getUpcomingEvents(user);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(JwtAuthenticationToken authToken,
                                            @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        priorityEventService.deleteEvent(user, id);
        return ResponseEntity.noContent().build();
    }
}
