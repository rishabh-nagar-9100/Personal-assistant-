package com.jarvis.timetable.service;

import com.jarvis.auth.model.User;
import com.jarvis.timetable.dto.BulkSlotRequest;
import com.jarvis.timetable.dto.CreateSlotRequest;
import com.jarvis.timetable.dto.FreeSlotResponse;
import com.jarvis.timetable.dto.SlotResponse;
import com.jarvis.timetable.model.SlotType;
import com.jarvis.timetable.model.TimetableSlot;
import com.jarvis.timetable.repository.TimetableSlotRepository;
import com.jarvis.timetable.repository.UserDailyStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private TimetableSlotRepository repository;

    @Mock
    private UserDailyStateRepository userDailyStateRepository;

    private TimetableService timetableService;
    private User testUser;

    @BeforeEach
    void setUp() {
        timetableService = new TimetableService(repository, userDailyStateRepository);
        testUser = new User(UUID.randomUUID(), "test@example.com");
    }

    @Test
    @DisplayName("Should create single timetable slot successfully")
    void testCreateSlot() {
        CreateSlotRequest request = new CreateSlotRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                SlotType.CLASS,
                "Data Structures Lecture"
        );

        when(repository.save(any(TimetableSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SlotResponse response = timetableService.createSlot(testUser, request);

        assertNotNull(response);
        assertEquals(DayOfWeek.MONDAY, response.dayOfWeek());
        assertEquals(LocalTime.of(9, 0), response.startTime());
        assertEquals(LocalTime.of(10, 30), response.endTime());
        assertEquals(SlotType.CLASS, response.type());
        assertEquals("Data Structures Lecture", response.label());
        verify(repository, times(1)).save(any(TimetableSlot.class));
    }

    @Test
    @DisplayName("Should fail when slot end time is before or equal to start time")
    void testInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () -> new TimetableSlot(
                testUser,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(9, 0),
                SlotType.CLASS,
                "Invalid Slot"
        ));
    }

    @Test
    @DisplayName("Should calculate free slots accurately around class schedules")
    void testGetFreeSlots() {
        TimetableSlot class1 = new TimetableSlot(
                testUser,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                SlotType.CLASS,
                "Operating Systems"
        );
        TimetableSlot class2 = new TimetableSlot(
                testUser,
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                SlotType.CLASS,
                "Database Systems"
        );

        when(repository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(testUser.getId(), DayOfWeek.MONDAY))
                .thenReturn(List.of(class1, class2));

        List<FreeSlotResponse> freeSlots = timetableService.getFreeSlots(testUser, DayOfWeek.MONDAY);

        // Expected free slots (day range 08:00 - 22:00):
        // 1. 08:00 - 09:00 (60 mins)
        // 2. 11:00 - 14:00 (180 mins)
        // 3. 16:00 - 22:00 (360 mins)
        assertEquals(3, freeSlots.size());

        assertEquals(LocalTime.of(8, 0), freeSlots.get(0).startTime());
        assertEquals(LocalTime.of(9, 0), freeSlots.get(0).endTime());
        assertEquals(60, freeSlots.get(0).durationMinutes());

        assertEquals(LocalTime.of(11, 0), freeSlots.get(1).startTime());
        assertEquals(LocalTime.of(14, 0), freeSlots.get(1).endTime());
        assertEquals(180, freeSlots.get(1).durationMinutes());

        assertEquals(LocalTime.of(16, 0), freeSlots.get(2).startTime());
        assertEquals(LocalTime.of(22, 0), freeSlots.get(2).endTime());
        assertEquals(360, freeSlots.get(2).durationMinutes());
    }

    @Test
    @DisplayName("Should bulk replace user timetable slots when replaceExisting is true")
    void testBulkAddSlotsReplace() {
        CreateSlotRequest slotReq = new CreateSlotRequest(
                DayOfWeek.TUESDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                SlotType.CLASS,
                "Algorithms"
        );
        BulkSlotRequest bulkRequest = new BulkSlotRequest(List.of(slotReq), true);

        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SlotResponse> responses = timetableService.bulkAddSlots(testUser, bulkRequest);

        verify(repository, times(1)).deleteByUserId(testUser.getId());
        verify(repository, times(1)).saveAll(anyList());
        assertEquals(1, responses.size());
        assertEquals("Algorithms", responses.get(0).label());
    }

    @Test
    @DisplayName("Should seed 5-day college timetable correctly")
    void testSeedCollegeTimetable() {
        when(repository.findByUserIdOrderByDayOrderAscStartTimeAsc(testUser.getId())).thenReturn(List.of());
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SlotResponse> slots = timetableService.seedCollegeTimetable(testUser);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        // Verify slots contain Day 1 through Day 5 courses
        assertTrue(slots.stream().anyMatch(s -> s.label().contains("Deep learning")));
        assertTrue(slots.stream().anyMatch(s -> s.label().contains("Industrial cert.")));
        assertTrue(slots.stream().anyMatch(s -> s.label().contains("Solar energy")));
        verify(repository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should calculate free slots for Day 2 correctly removing class hours")
    void testFreeSlotsForDay2() {
        TimetableSlot class1 = new TimetableSlot(testUser, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(8, 0), LocalTime.of(9, 40), SlotType.CLASS, "Deep learning");
        TimetableSlot class2 = new TimetableSlot(testUser, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(9, 45), LocalTime.of(11, 30), SlotType.CLASS, "Solar energy");
        TimetableSlot class3 = new TimetableSlot(testUser, DayOfWeek.TUESDAY, "DAY_2", LocalTime.of(11, 35), LocalTime.of(12, 25), SlotType.CLASS, "Industrial cert.");

        when(repository.findByUserIdAndDayOrderOrderByStartTimeAsc(testUser.getId(), "DAY_2"))
                .thenReturn(List.of(class1, class2, class3));

        List<FreeSlotResponse> freeSlots = timetableService.getFreeSlotsForDayOrder(testUser, "DAY_2", DayOfWeek.TUESDAY);

        assertNotNull(freeSlots);
        // Free block starting from 12:25/12:30 to 22:00
        assertTrue(freeSlots.stream().anyMatch(f -> f.endTime().equals(LocalTime.of(22, 0))));
    }
}
