package com.jarvis.topic.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.topic.dto.CreateSubjectRequest;
import com.jarvis.topic.dto.SubjectResponse;
import com.jarvis.topic.service.SubjectService;
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
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final UserService userService;

    public SubjectController(SubjectService subjectService, UserService userService) {
        this.subjectService = subjectService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(JwtAuthenticationToken authToken,
                                                         @RequestBody CreateSubjectRequest request) {
        User user = userService.getOrCreateUser(authToken);
        SubjectResponse response = subjectService.createSubject(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getUserSubjects(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<SubjectResponse> responses = subjectService.getUserSubjects(user);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<com.jarvis.topic.dto.SubjectSummaryResponse>> getSubjectSummaries(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<com.jarvis.topic.dto.SubjectSummaryResponse> summaries = subjectService.getSubjectSummaries(user);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<com.jarvis.practice.dto.SubjectQuestionResponse>> getSubjectQuestions(JwtAuthenticationToken authToken,
                                                                                                    @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        List<com.jarvis.practice.dto.SubjectQuestionResponse> questions = subjectService.getSubjectQuestions(user, id);
        return ResponseEntity.ok(questions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(JwtAuthenticationToken authToken,
                                               @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        subjectService.deleteSubject(user, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> clearAllSubjects(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        subjectService.clearAllUserData(user);
        return ResponseEntity.noContent().build();
    }
}
