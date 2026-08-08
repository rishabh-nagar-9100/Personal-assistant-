package com.jarvis.chat.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.chat.dto.ChatRequest;
import com.jarvis.chat.dto.ChatResponse;
import com.jarvis.chat.service.ChatAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatAgentController {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentController.class);

    private final ChatAgentService chatAgentService;
    private final UserService userService;

    public ChatAgentController(ChatAgentService chatAgentService, UserService userService) {
        this.chatAgentService = chatAgentService;
        this.userService = userService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request,
            JwtAuthenticationToken authToken) {
        try {
            User user = userService.getOrCreateUser(authToken);
            ChatResponse response = chatAgentService.processMessage(user, request.message());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in chat message processing", e);
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Sorry, I encountered an error processing your message. Please try again."));
        }
    }
}
