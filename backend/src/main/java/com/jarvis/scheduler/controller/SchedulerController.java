package com.jarvis.scheduler.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.service.SchedulerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/schedule")
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final UserService userService;

    public SchedulerController(SchedulerService schedulerService, UserService userService) {
        this.schedulerService = schedulerService;
        this.userService = userService;
    }

    @GetMapping("/today")
    public ResponseEntity<DailyScheduleResponse> getTodaySchedule(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        DailyScheduleResponse response = schedulerService.generateTodaySchedule(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date")
    public ResponseEntity<DailyScheduleResponse> getScheduleForDate(JwtAuthenticationToken authToken,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = userService.getOrCreateUser(authToken);
        DailyScheduleResponse response = schedulerService.generateScheduleForDate(user, date);
        return ResponseEntity.ok(response);
    }
}
