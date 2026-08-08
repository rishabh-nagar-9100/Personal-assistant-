package com.jarvis.timetable.controller;

import com.jarvis.auth.model.User;
import com.jarvis.auth.service.UserService;
import com.jarvis.timetable.dto.BulkSlotRequest;
import com.jarvis.timetable.dto.CreateSlotRequest;
import com.jarvis.timetable.dto.DayOrderInfoResponse;
import com.jarvis.timetable.dto.DayOrderSelectRequest;
import com.jarvis.timetable.dto.FreeSlotResponse;
import com.jarvis.timetable.dto.SlotResponse;
import com.jarvis.timetable.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final UserService userService;

    public TimetableController(TimetableService timetableService, UserService userService) {
        this.timetableService = timetableService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(JwtAuthenticationToken authToken,
                                                   @RequestBody CreateSlotRequest request) {
        User user = userService.getOrCreateUser(authToken);
        SlotResponse response = timetableService.createSlot(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<SlotResponse>> bulkAddSlots(JwtAuthenticationToken authToken,
                                                           @RequestBody BulkSlotRequest request) {
        User user = userService.getOrCreateUser(authToken);
        List<SlotResponse> responses = timetableService.bulkAddSlots(user, request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<SlotResponse>> getTimetable(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        // Ensure college timetable is seeded if empty
        timetableService.seedCollegeTimetable(user);
        List<SlotResponse> responses = timetableService.getUserTimetable(user);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<List<SlotResponse>> getTimetableForDay(JwtAuthenticationToken authToken,
                                                                 @PathVariable DayOfWeek day) {
        User user = userService.getOrCreateUser(authToken);
        List<SlotResponse> responses = timetableService.getUserTimetableForDay(user, day);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(JwtAuthenticationToken authToken,
                                           @PathVariable UUID id) {
        User user = userService.getOrCreateUser(authToken);
        timetableService.deleteSlot(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/free-slots")
    public ResponseEntity<List<FreeSlotResponse>> getFreeSlots(JwtAuthenticationToken authToken,
                                                               @RequestParam DayOfWeek day) {
        User user = userService.getOrCreateUser(authToken);
        List<FreeSlotResponse> responses = timetableService.getFreeSlots(user, day);
        return ResponseEntity.ok(responses);
    }

    /* ─── 5-Day Order College Timetable Endpoints ─── */

    @PostMapping("/seed-college")
    public ResponseEntity<List<SlotResponse>> seedCollegeTimetable(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        List<SlotResponse> slots = timetableService.seedCollegeTimetable(user);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/day-order/current")
    public ResponseEntity<DayOrderInfoResponse> getCurrentDayOrder(JwtAuthenticationToken authToken) {
        User user = userService.getOrCreateUser(authToken);
        timetableService.seedCollegeTimetable(user);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String activeDayOrder = timetableService.getActiveDayOrder(user, today);
        List<SlotResponse> classSlots = timetableService.getSlotsForDayOrder(user, activeDayOrder);
        List<FreeSlotResponse> freeSlots = timetableService.getFreeSlotsForDayOrder(user, activeDayOrder, today.getDayOfWeek());

        DayOrderInfoResponse response = new DayOrderInfoResponse(
                activeDayOrder,
                today,
                !classSlots.isEmpty() || activeDayOrder.equalsIgnoreCase("HOLIDAY"),
                classSlots,
                freeSlots
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/day-order/select")
    public ResponseEntity<DayOrderInfoResponse> selectDayOrder(JwtAuthenticationToken authToken,
                                                               @RequestBody DayOrderSelectRequest request) {
        User user = userService.getOrCreateUser(authToken);
        timetableService.seedCollegeTimetable(user);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String chosen = timetableService.setActiveDayOrder(user, today, request.dayOrder());

        List<SlotResponse> classSlots = timetableService.getSlotsForDayOrder(user, chosen);
        List<FreeSlotResponse> freeSlots = timetableService.getFreeSlotsForDayOrder(user, chosen, today.getDayOfWeek());

        DayOrderInfoResponse response = new DayOrderInfoResponse(
                chosen,
                today,
                true,
                classSlots,
                freeSlots
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/day-order/slots")
    public ResponseEntity<List<SlotResponse>> getSlotsByDayOrder(JwtAuthenticationToken authToken,
                                                                 @RequestParam String dayOrder) {
        User user = userService.getOrCreateUser(authToken);
        List<SlotResponse> responses = timetableService.getSlotsForDayOrder(user, dayOrder);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/day-order/free-slots")
    public ResponseEntity<List<FreeSlotResponse>> getFreeSlotsByDayOrder(JwtAuthenticationToken authToken,
                                                                         @RequestParam String dayOrder) {
        User user = userService.getOrCreateUser(authToken);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<FreeSlotResponse> responses = timetableService.getFreeSlotsForDayOrder(user, dayOrder, today.getDayOfWeek());
        return ResponseEntity.ok(responses);
    }
}
