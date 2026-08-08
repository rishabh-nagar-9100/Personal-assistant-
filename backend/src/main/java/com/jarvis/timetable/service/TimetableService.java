package com.jarvis.timetable.service;

import com.jarvis.auth.model.User;
import com.jarvis.timetable.dto.BulkSlotRequest;
import com.jarvis.timetable.dto.CreateSlotRequest;
import com.jarvis.timetable.dto.FreeSlotResponse;
import com.jarvis.timetable.dto.SlotResponse;
import com.jarvis.timetable.model.SlotType;
import com.jarvis.timetable.model.TimetableSlot;
import com.jarvis.timetable.model.UserDailyState;
import com.jarvis.timetable.repository.TimetableSlotRepository;
import com.jarvis.timetable.repository.UserDailyStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimetableService {

    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(22, 0);

    private final TimetableSlotRepository repository;
    private final UserDailyStateRepository userDailyStateRepository;

    public TimetableService(TimetableSlotRepository repository, UserDailyStateRepository userDailyStateRepository) {
        this.repository = repository;
        this.userDailyStateRepository = userDailyStateRepository;
    }

    @Transactional
    public SlotResponse createSlot(User user, CreateSlotRequest request) {
        TimetableSlot slot = new TimetableSlot(
                user,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.type(),
                request.label()
        );
        TimetableSlot saved = repository.save(slot);
        return SlotResponse.fromEntity(saved);
    }

    @Transactional
    public List<SlotResponse> bulkAddSlots(User user, BulkSlotRequest request) {
        if (request.replaceExisting()) {
            repository.deleteByUserId(user.getId());
        }

        List<TimetableSlot> slotsToSave = request.slots().stream()
                .map(req -> new TimetableSlot(
                        user,
                        req.dayOfWeek(),
                        req.startTime(),
                        req.endTime(),
                        req.type(),
                        req.label()
                ))
                .toList();

        List<TimetableSlot> saved = repository.saveAll(slotsToSave);
        return saved.stream().map(SlotResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getUserTimetable(User user) {
        return repository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(user.getId())
                .stream()
                .map(SlotResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getUserTimetableForDay(User user, DayOfWeek day) {
        return repository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(user.getId(), day)
                .stream()
                .map(SlotResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsForDayOrder(User user, String dayOrder) {
        if (dayOrder == null || dayOrder.equalsIgnoreCase("HOLIDAY") || dayOrder.equalsIgnoreCase("WEEKEND")) {
            return List.of();
        }
        return repository.findByUserIdAndDayOrderOrderByStartTimeAsc(user.getId(), dayOrder.toUpperCase())
                .stream()
                .map(SlotResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteSlot(User user, UUID slotId) {
        TimetableSlot slot = repository.findByIdAndUserId(slotId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Timetable slot not found with id: " + slotId));
        repository.delete(slot);
    }

    /**
     * Seeds the user's exact 5-Day College Timetable.
     */
    @Transactional
    public List<SlotResponse> seedCollegeTimetable(User user) {
        List<TimetableSlot> existing = repository.findByUserIdOrderByDayOrderAscStartTimeAsc(user.getId());
        if (!existing.isEmpty()) {
            return existing.stream().map(SlotResponse::fromEntity).toList();
        }

        List<TimetableSlot> slots = new ArrayList<>();

        // Day 1
        slots.add(new TimetableSlot(user, DayOfWeek.MONDAY, "DAY_1", LocalTime.of(12, 30), LocalTime.of(13, 20), SlotType.CLASS, "A - Industrial cert."));
        slots.add(new TimetableSlot(user, DayOfWeek.MONDAY, "DAY_1", LocalTime.of(13, 25), LocalTime.of(14, 15), SlotType.CLASS, "A - Industrial cert."));
        slots.add(new TimetableSlot(user, DayOfWeek.MONDAY, "DAY_1", LocalTime.of(16, 0), LocalTime.of(16, 50), SlotType.CLASS, "G - Solar energy"));
        slots.add(new TimetableSlot(user, DayOfWeek.MONDAY, "DAY_1", LocalTime.of(16, 50), LocalTime.of(17, 30), SlotType.CLASS, "L11 - Behavioral psychology"));
        slots.add(new TimetableSlot(user, DayOfWeek.MONDAY, "DAY_1", LocalTime.of(17, 30), LocalTime.of(18, 10), SlotType.CLASS, "L12 - Behavioral psychology"));

        // Day 2
        slots.add(new TimetableSlot(user, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(8, 0), LocalTime.of(8, 50), SlotType.CLASS, "B - Deep learning"));
        slots.add(new TimetableSlot(user, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(8, 50), LocalTime.of(9, 40), SlotType.CLASS, "B/X - Deep learning"));
        slots.add(new TimetableSlot(user, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(9, 45), LocalTime.of(10, 35), SlotType.CLASS, "G/X - Solar energy"));
        slots.add(new TimetableSlot(user, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(10, 40), LocalTime.of(11, 30), SlotType.CLASS, "G - Solar energy"));
        slots.add(new TimetableSlot(user, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(11, 35), LocalTime.of(12, 25), SlotType.CLASS, "A - Industrial cert."));

        // Day 3
        slots.add(new TimetableSlot(user, DayOfWeek.WEDNESDAY, "DAY_3", LocalTime.of(12, 30), LocalTime.of(13, 20), SlotType.CLASS, "C - SW measurements & metrics"));
        slots.add(new TimetableSlot(user, DayOfWeek.WEDNESDAY, "DAY_3", LocalTime.of(13, 25), LocalTime.of(14, 15), SlotType.CLASS, "C - SW measurements & metrics"));
        slots.add(new TimetableSlot(user, DayOfWeek.WEDNESDAY, "DAY_3", LocalTime.of(14, 20), LocalTime.of(15, 10), SlotType.CLASS, "A - Industrial cert."));
        slots.add(new TimetableSlot(user, DayOfWeek.WEDNESDAY, "DAY_3", LocalTime.of(15, 10), LocalTime.of(16, 0), SlotType.CLASS, "D - Software V&V"));
        slots.add(new TimetableSlot(user, DayOfWeek.WEDNESDAY, "DAY_3", LocalTime.of(16, 0), LocalTime.of(16, 50), SlotType.CLASS, "B - Deep learning"));

        // Day 4
        slots.add(new TimetableSlot(user, DayOfWeek.THURSDAY, "DAY_4", LocalTime.of(8, 0), LocalTime.of(8, 50), SlotType.CLASS, "D - Software V&V"));
        slots.add(new TimetableSlot(user, DayOfWeek.THURSDAY, "DAY_4", LocalTime.of(8, 50), LocalTime.of(9, 40), SlotType.CLASS, "D/X - Software V&V"));
        slots.add(new TimetableSlot(user, DayOfWeek.THURSDAY, "DAY_4", LocalTime.of(9, 45), LocalTime.of(10, 35), SlotType.CLASS, "B/X - Deep learning"));
        slots.add(new TimetableSlot(user, DayOfWeek.THURSDAY, "DAY_4", LocalTime.of(11, 35), LocalTime.of(12, 25), SlotType.CLASS, "C - SW measurements & metrics"));

        // Day 5
        slots.add(new TimetableSlot(user, DayOfWeek.FRIDAY, "DAY_5", LocalTime.of(14, 20), LocalTime.of(15, 10), SlotType.CLASS, "C - SW measurements & metrics"));
        slots.add(new TimetableSlot(user, DayOfWeek.FRIDAY, "DAY_5", LocalTime.of(16, 0), LocalTime.of(16, 50), SlotType.CLASS, "D - Software V&V"));

        List<TimetableSlot> saved = repository.saveAll(slots);
        return saved.stream().map(SlotResponse::fromEntity).toList();
    }

    /**
     * Retrieves or sets the active Day Order for a given date.
     */
    @Transactional(readOnly = true)
    public String getActiveDayOrder(User user, LocalDate date) {
        Optional<UserDailyState> state = userDailyStateRepository.findByUserIdAndDate(user.getId(), date);
        if (state.isPresent()) {
            return state.get().getDayOrder();
        }
        // Default smart fallback based on day of week
        DayOfWeek dow = date.getDayOfWeek();
        return switch (dow) {
            case MONDAY -> "DAY_1";
            case TUESDAY -> "DAY_2";
            case WEDNESDAY -> "DAY_3";
            case THURSDAY -> "DAY_4";
            case FRIDAY -> "DAY_5";
            case SATURDAY, SUNDAY -> "HOLIDAY";
        };
    }

    @Transactional
    public String setActiveDayOrder(User user, LocalDate date, String dayOrder) {
        String clean = dayOrder != null ? dayOrder.trim().toUpperCase() : "DAY_1";
        Optional<UserDailyState> state = userDailyStateRepository.findByUserIdAndDate(user.getId(), date);
        if (state.isPresent()) {
            UserDailyState s = state.get();
            s.setDayOrder(clean);
            userDailyStateRepository.save(s);
        } else {
            UserDailyState s = new UserDailyState(user, date, clean);
            userDailyStateRepository.save(s);
        }
        return clean;
    }

    /**
     * Calculates free study blocks for Day Order by subtracting college classes.
     */
    @Transactional(readOnly = true)
    public List<FreeSlotResponse> getFreeSlotsForDayOrder(User user, String dayOrder, DayOfWeek day) {
        if (dayOrder == null || dayOrder.equalsIgnoreCase("HOLIDAY") || dayOrder.equalsIgnoreCase("WEEKEND")) {
            long totalMinutes = Duration.between(DAY_START, DAY_END).toMinutes();
            return List.of(new FreeSlotResponse(day != null ? day : DayOfWeek.MONDAY, DAY_START, DAY_END, totalMinutes));
        }

        List<TimetableSlot> busySlots = repository.findByUserIdAndDayOrderOrderByStartTimeAsc(user.getId(), dayOrder.toUpperCase())
                .stream()
                .filter(s -> s.getType() == SlotType.CLASS || s.getType() == SlotType.FIXED_COMMITMENT)
                .sorted(Comparator.comparing(TimetableSlot::getStartTime))
                .toList();

        List<FreeSlotResponse> freeSlots = new ArrayList<>();
        LocalTime currentStart = DAY_START;

        for (TimetableSlot busy : busySlots) {
            LocalTime busyStart = busy.getStartTime();
            LocalTime busyEnd = busy.getEndTime();

            if (busyStart.isAfter(currentStart)) {
                LocalTime freeEnd = busyStart.isBefore(DAY_END) ? busyStart : DAY_END;
                if (freeEnd.isAfter(currentStart)) {
                    long duration = Duration.between(currentStart, freeEnd).toMinutes();
                    if (duration >= 15) { // at least 15 min study window
                        freeSlots.add(new FreeSlotResponse(day != null ? day : DayOfWeek.MONDAY, currentStart, freeEnd, duration));
                    }
                }
            }

            if (busyEnd.isAfter(currentStart)) {
                currentStart = busyEnd;
            }
        }

        if (currentStart.isBefore(DAY_END)) {
            long duration = Duration.between(currentStart, DAY_END).toMinutes();
            if (duration >= 15) {
                freeSlots.add(new FreeSlotResponse(day != null ? day : DayOfWeek.MONDAY, currentStart, DAY_END, duration));
            }
        }

        return freeSlots;
    }

    @Transactional(readOnly = true)
    public List<FreeSlotResponse> getFreeSlots(User user, DayOfWeek day) {
        List<TimetableSlot> daySlots = repository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(user.getId(), day);
        if (!daySlots.isEmpty()) {
            return calculateFreeSlotsFromBusyList(daySlots, day);
        }
        String activeDayOrder = getActiveDayOrder(user, LocalDate.now());
        return getFreeSlotsForDayOrder(user, activeDayOrder, day);
    }

    private List<FreeSlotResponse> calculateFreeSlotsFromBusyList(List<TimetableSlot> allSlots, DayOfWeek day) {
        List<TimetableSlot> busySlots = allSlots.stream()
                .filter(s -> s.getType() == SlotType.CLASS || s.getType() == SlotType.FIXED_COMMITMENT)
                .sorted(Comparator.comparing(TimetableSlot::getStartTime))
                .toList();

        List<FreeSlotResponse> freeSlots = new ArrayList<>();
        LocalTime currentStart = DAY_START;

        for (TimetableSlot busy : busySlots) {
            LocalTime busyStart = busy.getStartTime();
            LocalTime busyEnd = busy.getEndTime();

            if (busyStart.isAfter(currentStart)) {
                LocalTime freeEnd = busyStart.isBefore(DAY_END) ? busyStart : DAY_END;
                if (freeEnd.isAfter(currentStart)) {
                    long duration = Duration.between(currentStart, freeEnd).toMinutes();
                    if (duration > 0) {
                        freeSlots.add(new FreeSlotResponse(day, currentStart, freeEnd, duration));
                    }
                }
            }

            if (busyEnd.isAfter(currentStart)) {
                currentStart = busyEnd;
            }
        }

        if (currentStart.isBefore(DAY_END)) {
            long duration = Duration.between(currentStart, DAY_END).toMinutes();
            if (duration > 0) {
                freeSlots.add(new FreeSlotResponse(day, currentStart, DAY_END, duration));
            }
        }

        return freeSlots;
    }
}
