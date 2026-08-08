package com.jarvis.task.service;

import com.jarvis.auth.model.User;
import com.jarvis.task.dto.CreatePriorityEventRequest;
import com.jarvis.task.dto.PriorityEventResponse;
import com.jarvis.task.model.PriorityEvent;
import com.jarvis.task.repository.PriorityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class PriorityEventService {

    private final PriorityEventRepository priorityEventRepository;

    public PriorityEventService(PriorityEventRepository priorityEventRepository) {
        this.priorityEventRepository = priorityEventRepository;
    }

    @Transactional
    public PriorityEventResponse createEvent(User user, CreatePriorityEventRequest request) {
        PriorityEvent event = new PriorityEvent(
                user,
                request.name(),
                request.eventDate(),
                request.type(),
                request.jdText(),
                request.boostedTopicIds()
        );
        PriorityEvent saved = priorityEventRepository.save(event);
        return PriorityEventResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PriorityEventResponse> getAllEvents(User user) {
        return priorityEventRepository.findByUserIdOrderByEventDateAsc(user.getId())
                .stream()
                .map(PriorityEventResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PriorityEventResponse> getUpcomingEvents(User user) {
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(user.getId(), startOfToday)
                .stream()
                .map(PriorityEventResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteEvent(User user, UUID eventId) {
        PriorityEvent event = priorityEventRepository.findByIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Priority event not found with id: " + eventId));
        priorityEventRepository.delete(event);
    }
}
