package com.jarvis.dashboard.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.dashboard.dto.DashboardMetricsResponse;
import com.jarvis.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        return ResponseEntity.ok(dashboardService.getMetrics(user));
    }
}
