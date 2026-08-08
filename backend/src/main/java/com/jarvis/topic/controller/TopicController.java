package com.jarvis.topic.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.topic.dto.CreateTopicRequest;
import com.jarvis.topic.dto.ReviewTopicRequest;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.service.TopicService;
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
@RequestMapping("/topics")
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;

    public TopicController(TopicService topicService, UserService userService) {
        this.topicService = topicService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(JwtAuthenticationToken authToken,
                                                     @RequestBody CreateTopicRequest request) {
        User user = userService.getOrCreateUser(authToken);
        TopicResponse response = topicService.createTopic(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<TopicResponse>> getTopicsBySubject(JwtAuthenticationToken authToken,
                                                                   @PathVariable UUID subjectId) {
        User user = userService.getOrCreateUser(authToken);
        List<TopicResponse> responses = topicService.getTopicsBySubject(user, subjectId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<TopicResponse> reviewTopic(JwtAuthenticationToken authToken,
                                                     @PathVariable UUID id,
                                                     @RequestBody ReviewTopicRequest request) {
        User user = userService.getOrCreateUser(authToken);
        TopicResponse response = topicService.reviewTopic(user, id, request.quality());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/due-for-revision")
    public ResponseEntity<List<TopicResponse>> getTopicsDueForRevision(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<TopicResponse> responses = topicService.getTopicsDueForRevision(user);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(JwtAuthenticationToken authToken,
                                            @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        topicService.deleteTopic(user, id);
        return ResponseEntity.noContent().build();
    }
}
