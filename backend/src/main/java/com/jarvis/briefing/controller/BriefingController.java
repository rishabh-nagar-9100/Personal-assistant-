package com.jarvis.briefing.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.briefing.dto.DailyBriefingResponse;
import com.jarvis.briefing.service.BriefingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/briefing")
public class BriefingController {

    private final BriefingService briefingService;
    private final UserService userService;

    public BriefingController(BriefingService briefingService, UserService userService) {
        this.briefingService = briefingService;
        this.userService = userService;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BriefingController.class);

    @GetMapping("/today")
    public ResponseEntity<DailyBriefingResponse> getTodayBriefing(JwtAuthenticationToken authToken) {
        try {
            User user = userService.getOrCreateUser(authToken);
            DailyBriefingResponse response = briefingService.getTodayBriefing(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in getTodayBriefing", e);
            throw e;
        }
    }

    @PostMapping("/today/regenerate")
    public ResponseEntity<DailyBriefingResponse> regenerateTodayBriefing(JwtAuthenticationToken authToken) {
        try {
            User user = userService.getOrCreateUser(authToken);
            DailyBriefingResponse response = briefingService.regenerateTodayBriefing(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in regenerateTodayBriefing", e);
            throw e;
        }
    }
}
