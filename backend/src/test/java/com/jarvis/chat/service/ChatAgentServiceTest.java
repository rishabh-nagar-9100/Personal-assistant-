package com.jarvis.chat.service;

import com.jarvis.auth.model.User;
import com.jarvis.briefing.client.LlmClient;
import com.jarvis.chat.dto.ChatResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.service.PracticeService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.dto.ScheduledSlotItem;
import com.jarvis.scheduler.model.ScheduledItemType;
import com.jarvis.scheduler.service.SchedulerService;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.model.TaskPriority;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.service.TaskService;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.model.TopicStatus;
import com.jarvis.topic.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jarvis.timetable.service.TimetableService;

@ExtendWith(MockitoExtension.class)
class ChatAgentServiceTest {

    @Mock private LlmClient llmClient;
    @Mock private SchedulerService schedulerService;
    @Mock private TopicService topicService;
    @Mock private TaskService taskService;
    @Mock private PracticeService practiceService;
    @Mock private com.jarvis.topic.service.SubjectService subjectService;
    @Mock private TimetableService timetableService;

    private ChatAgentService chatAgentService;
    private User testUser;

    @BeforeEach
    void setUp() {
        chatAgentService = new ChatAgentService(
                llmClient, schedulerService, topicService, taskService, practiceService, subjectService, timetableService
        );
        testUser = new User(UUID.randomUUID(), "rishabh@example.com");
    }

    // ─── Helper methods to build mock data ───

    private void setupStandardMocks() {
        // Schedule mock
        when(schedulerService.generateTodaySchedule(any(User.class))).thenReturn(
                new DailyScheduleResponse(
                        LocalDate.now(),
                        DayOfWeek.FRIDAY,
                        List.of(),
                        List.of(
                                new ScheduledSlotItem(
                                        LocalTime.of(9, 0), LocalTime.of(10, 0), 60,
                                        ScheduledItemType.DSA_PRACTICE, "DSA Practice", "Binary Search", null
                                ),
                                new ScheduledSlotItem(
                                        LocalTime.of(10, 0), LocalTime.of(11, 0), 60,
                                        ScheduledItemType.TOPIC_REVISION, "Revision: OS Paging", "Operating Systems", null
                                )
                        ),
                        List.of(),
                        false,
                        "Your Friday schedule"
                )
        );

        // Due topics mock
        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(
                List.of(
                        new TopicResponse(
                                UUID.randomUUID(), UUID.randomUUID(), "Operating Systems",
                                "Paging & Virtual Memory", TopicStatus.IN_PROGRESS,
                                Instant.now(), Instant.now(), 2.3, 3, 2
                        ),
                        new TopicResponse(
                                UUID.randomUUID(), UUID.randomUUID(), "DBMS",
                                "Normalization", TopicStatus.IN_PROGRESS,
                                Instant.now(), Instant.now(), 1.8, 1, 1
                        )
                )
        );

        // Practice quotas mock
        when(practiceService.getTodayQuotaStatus(any(User.class))).thenReturn(
                new TodayQuotaResponse(LocalDate.now(), 5, 3, 2, 5, 1, 4, 5, 0, 5)
        );

        // Tasks mock
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(
                List.of(
                        new TaskResponse(UUID.randomUUID(), "Submit Resume", "Update resume",
                                Instant.now(), TaskPriority.HIGH, null, TaskStatus.PENDING),
                        new TaskResponse(UUID.randomUUID(), "Read Chapter 5", "DBMS textbook",
                                Instant.now(), TaskPriority.MEDIUM, null, TaskStatus.DONE)
                )
        );
    }

    // ─── Intent Classification Tests ───

    @Test
    @DisplayName("Schedule query intent — deterministic fallback includes schedule data")
    void testScheduleQueryIntent() {
        setupStandardMocks();
        // LLM call will throw to trigger deterministic fallback
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "Show me my schedule for today");

        assertNotNull(response);
        assertEquals("SCHEDULE_QUERY", response.intent());
        assertTrue(response.actionExecuted());
        assertTrue(response.reply().toLowerCase().contains("schedule"), "Reply should contain schedule info");
    }

    @Test
    @DisplayName("Study recommendation intent — deterministic fallback includes revision queue")
    void testStudyRecommendationIntent() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "What should I study today?");

        assertNotNull(response);
        assertEquals("STUDY_RECOMMENDATION", response.intent());
        assertTrue(response.actionExecuted());
        assertTrue(response.reply().contains("revision queue") || response.reply().contains("REVISION QUEUE"),
                "Reply should reference revision queue");
    }

    @Test
    @DisplayName("Progress check intent — deterministic fallback includes quota data")
    void testProgressCheckIntent() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "How is my progress today?");

        assertNotNull(response);
        assertEquals("PROGRESS_CHECK", response.intent());
        assertTrue(response.reply().contains("DSA") || response.reply().contains("PRACTICE"),
                "Reply should contain practice quota info");
    }

    @Test
    @DisplayName("Priority event intent — deterministic fallback offers prioritization advice")
    void testPriorityEventIntent() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "I have an exam on Friday");

        assertNotNull(response);
        assertEquals("PRIORITY_EVENT", response.intent());
        assertTrue(response.reply().contains("prioritize") || response.reply().contains("important"),
                "Reply should offer prioritization advice");
    }

    @Test
    @DisplayName("Task management intent — deterministic fallback includes task overview")
    void testTaskManagementIntent() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "Show my tasks and todos");

        assertNotNull(response);
        assertEquals("TASK_MANAGEMENT", response.intent());
        assertTrue(response.reply().contains("task") || response.reply().contains("TASKS"),
                "Reply should contain task info");
    }

    @Test
    @DisplayName("General chat intent — deterministic fallback offers help options")
    void testGeneralChatIntent() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "hello there");

        assertNotNull(response);
        assertEquals("GENERAL_CHAT", response.intent());
        assertTrue(response.reply().contains("JARVIS"), "Reply should introduce JARVIS");
        assertTrue(response.reply().contains("What should I study"), "Reply should offer example prompts");
    }

    // ─── LLM Success Path ───

    @Test
    @DisplayName("When LLM succeeds — returns LLM-generated response with correct intent")
    void testLlmSuccessPath() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenReturn(
                "Based on your schedule, I recommend studying Operating Systems next."
        );

        ChatResponse response = chatAgentService.processMessage(testUser, "What should I study?");

        assertNotNull(response);
        assertEquals("STUDY_RECOMMENDATION", response.intent());
        assertEquals("Based on your schedule, I recommend studying Operating Systems next.", response.reply());
    }

    // ─── Graceful Degradation Tests ───

    @Test
    @DisplayName("Handles schedule service failure gracefully in context building")
    void testScheduleServiceFailure() {
        when(schedulerService.generateTodaySchedule(any(User.class)))
                .thenThrow(new RuntimeException("DB connection timeout"));
        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(List.of());
        when(practiceService.getTodayQuotaStatus(any(User.class)))
                .thenReturn(new TodayQuotaResponse(LocalDate.now(), 5, 0, 5, 5, 0, 5, 5, 0, 5));
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(List.of());
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "Show my schedule");

        // Should not throw — should gracefully degrade
        assertNotNull(response);
        assertEquals("SCHEDULE_QUERY", response.intent());
    }

    @Test
    @DisplayName("Actions performed list is non-empty in deterministic mode")
    void testActionsPerformedInDeterministicMode() {
        setupStandardMocks();
        when(llmClient.generateBriefingText(any(), any())).thenThrow(new RuntimeException("LLM unavailable"));

        ChatResponse response = chatAgentService.processMessage(testUser, "hello");

        assertNotNull(response.actionsPerformed());
        assertFalse(response.actionsPerformed().isEmpty());
        assertTrue(response.actionsPerformed().get(0).contains("deterministic"),
                "Should indicate deterministic mode in actions");
    }
}
