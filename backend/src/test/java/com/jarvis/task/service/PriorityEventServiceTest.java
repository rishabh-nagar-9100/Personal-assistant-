package com.jarvis.task.service;

import com.jarvis.auth.model.User;
import com.jarvis.task.dto.CreatePriorityEventRequest;
import com.jarvis.task.dto.PriorityEventResponse;
import com.jarvis.task.model.PriorityEvent;
import com.jarvis.task.model.PriorityEventType;
import com.jarvis.task.repository.PriorityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriorityEventServiceTest {

    @Mock
    private PriorityEventRepository priorityEventRepository;

    private PriorityEventService priorityEventService;
    private User testUser;

    @BeforeEach
    void setUp() {
        priorityEventService = new PriorityEventService(priorityEventRepository);
        testUser = new User(UUID.randomUUID(), "eventuser@example.com");
    }

    @Test
    @DisplayName("Should create priority event successfully")
    void testCreateEvent() {
        Instant eventDate = Instant.now().plus(7, ChronoUnit.DAYS);
        CreatePriorityEventRequest request = new CreatePriorityEventRequest(
                "Google Online Assessment",
                eventDate,
                PriorityEventType.PLACEMENT_TEST,
                "Requires DSA + System Design",
                List.of("dsa-trees", "dsa-graphs")
        );

        when(priorityEventRepository.save(any(PriorityEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriorityEventResponse response = priorityEventService.createEvent(testUser, request);

        assertNotNull(response);
        assertEquals("Google Online Assessment", response.name());
        assertEquals(PriorityEventType.PLACEMENT_TEST, response.type());
        assertEquals(2, response.boostedTopicIds().size());
    }

    @Test
    @DisplayName("Should retrieve upcoming priority events")
    void testGetUpcomingEvents() {
        Instant futureDate = Instant.now().plus(3, ChronoUnit.DAYS);
        PriorityEvent event = new PriorityEvent(testUser, "Midterm Exam", futureDate, PriorityEventType.EXAM, null, null);

        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(event));

        List<PriorityEventResponse> upcoming = priorityEventService.getUpcomingEvents(testUser);

        assertEquals(1, upcoming.size());
        assertEquals("Midterm Exam", upcoming.get(0).name());
    }
}
