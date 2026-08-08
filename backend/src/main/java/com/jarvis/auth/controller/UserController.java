package com.jarvis.auth.controller;

import com.jarvis.auth.dto.UserResponse;
import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
        return ResponseEntity.ok(response);
    }
}
