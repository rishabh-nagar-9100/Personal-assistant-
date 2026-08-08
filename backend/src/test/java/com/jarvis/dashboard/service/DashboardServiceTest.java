package com.jarvis.dashboard.service;

import com.jarvis.auth.model.User;
import com.jarvis.dashboard.dto.DashboardMetricsResponse;
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

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private SchedulerService schedulerService;
    @Mock private TopicService topicService;
    @Mock private TaskService taskService;
    @Mock private PracticeService practiceService;

    private DashboardService dashboardService;
    private User testUser;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                schedulerService, topicService, taskService, practiceService
        );
        testUser = new User(UUID.randomUUID(), "rishabh@example.com");
    }

    @Test
    @DisplayName("Should aggregate all metrics correctly from all services")
    void testGetMetrics_AllServicesHealthy() {
        // Practice quotas
        when(practiceService.getTodayQuotaStatus(any(User.class))).thenReturn(
                new TodayQuotaResponse(LocalDate.now(), 5, 3, 2, 5, 2, 3, 5, 1, 4)
        );

        // Tasks: 3 total (2 DONE, 1 PENDING)
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(List.of(
                new TaskResponse(UUID.randomUUID(), "Task A", null, null, TaskPriority.HIGH, null, TaskStatus.DONE),
                new TaskResponse(UUID.randomUUID(), "Task B", null, null, TaskPriority.MEDIUM, null, TaskStatus.DONE),
                new TaskResponse(UUID.randomUUID(), "Task C", null, null, TaskPriority.LOW, null, TaskStatus.PENDING)
        ));

        // Revision queue: 2 topics due
        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(List.of(
                new TopicResponse(UUID.randomUUID(), UUID.randomUUID(), "OS", "Paging",
                        TopicStatus.IN_PROGRESS, Instant.now(), Instant.now(), 2.3, 3, 2),
                new TopicResponse(UUID.randomUUID(), UUID.randomUUID(), "DBMS", "Normalization",
                        TopicStatus.IN_PROGRESS, Instant.now(), Instant.now(), 1.8, 1, 1)
        ));

        // Schedule: 2 items, total 120 minutes
        when(schedulerService.generateTodaySchedule(any(User.class))).thenReturn(
                new DailyScheduleResponse(
                        LocalDate.now(), DayOfWeek.FRIDAY, List.of(),
                        List.of(
                                new ScheduledSlotItem(LocalTime.of(9, 0), LocalTime.of(10, 0), 60,
                                        ScheduledItemType.DSA_PRACTICE, "DSA Practice", "Binary Search", null),
                                new ScheduledSlotItem(LocalTime.of(10, 0), LocalTime.of(11, 0), 60,
                                        ScheduledItemType.TOPIC_REVISION, "Revision", "OS", null)
                        ),
                        List.of(), false, "Summary"
                )
        );

        DashboardMetricsResponse metrics = dashboardService.getMetrics(testUser);

        assertNotNull(metrics);
        // Practice quotas
        assertEquals(3, metrics.dsaDone());
        assertEquals(5, metrics.dsaTarget());
        assertEquals(2, metrics.sqlDone());
        assertEquals(5, metrics.sqlTarget());
        assertEquals(1, metrics.aptitudeDone());
        assertEquals(5, metrics.aptitudeTarget());
        // Tasks
        assertEquals(2, metrics.tasksCompletedCount());
        assertEquals(3, metrics.tasksTotalCount());
        // Revision queue
        assertEquals(2, metrics.revisionQueueSize());
        // Schedule
        assertEquals(2, metrics.scheduledItemsCount());
        assertEquals(120, metrics.studyTimeMinutes());
        // Topics studied = dsaDone + sqlDone + aptDone = 3 + 2 + 1 = 6
        assertEquals(6, metrics.topicsStudiedCount());
    }

    @Test
    @DisplayName("Should handle practice service failure gracefully with defaults")
    void testGetMetrics_PracticeServiceFails() {
        when(practiceService.getTodayQuotaStatus(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(List.of());
        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(List.of());
        when(schedulerService.generateTodaySchedule(any(User.class))).thenReturn(
                new DailyScheduleResponse(LocalDate.now(), DayOfWeek.FRIDAY, List.of(),
                        List.of(), List.of(), false, "")
        );

        DashboardMetricsResponse metrics = dashboardService.getMetrics(testUser);

        assertNotNull(metrics);
        // Defaults when practice service fails
        assertEquals(0, metrics.dsaDone());
        assertEquals(5, metrics.dsaTarget());
        assertEquals(0, metrics.sqlDone());
        assertEquals(5, metrics.sqlTarget());
    }

    @Test
    @DisplayName("Should handle all services failing gracefully with zero defaults")
    void testGetMetrics_AllServicesFail() {
        when(practiceService.getTodayQuotaStatus(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));
        when(taskService.getUserTasks(any(User.class), isNull()))
                .thenThrow(new RuntimeException("DB error"));
        when(topicService.getTopicsDueForRevision(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));
        when(schedulerService.generateTodaySchedule(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));

        DashboardMetricsResponse metrics = dashboardService.getMetrics(testUser);

        // Should not throw — returns zeroed-out defaults
        assertNotNull(metrics);
        assertEquals(0, metrics.studyTimeMinutes());
        assertEquals(0, metrics.tasksCompletedCount());
        assertEquals(0, metrics.tasksTotalCount());
        assertEquals(0, metrics.revisionQueueSize());
        assertEquals(0, metrics.scheduledItemsCount());
    }

    @Test
    @DisplayName("Should correctly count DONE tasks using enum comparison, not string")
    void testTaskStatusEnumComparison() {
        when(practiceService.getTodayQuotaStatus(any(User.class))).thenReturn(
                new TodayQuotaResponse(LocalDate.now(), 5, 0, 5, 5, 0, 5, 5, 0, 5)
        );

        // 5 tasks: 3 DONE, 2 PENDING — verifying enum comparison works correctly
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(List.of(
                new TaskResponse(UUID.randomUUID(), "A", null, null, TaskPriority.HIGH, null, TaskStatus.DONE),
                new TaskResponse(UUID.randomUUID(), "B", null, null, TaskPriority.HIGH, null, TaskStatus.DONE),
                new TaskResponse(UUID.randomUUID(), "C", null, null, TaskPriority.HIGH, null, TaskStatus.DONE),
                new TaskResponse(UUID.randomUUID(), "D", null, null, TaskPriority.MEDIUM, null, TaskStatus.PENDING),
                new TaskResponse(UUID.randomUUID(), "E", null, null, TaskPriority.LOW, null, TaskStatus.PENDING)
        ));

        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(List.of());
        when(schedulerService.generateTodaySchedule(any(User.class))).thenReturn(
                new DailyScheduleResponse(LocalDate.now(), DayOfWeek.FRIDAY, List.of(),
                        List.of(), List.of(), false, "")
        );

        DashboardMetricsResponse metrics = dashboardService.getMetrics(testUser);

        assertEquals(3, metrics.tasksCompletedCount(), "Should correctly count 3 DONE tasks using enum comparison");
        assertEquals(5, metrics.tasksTotalCount());
    }

    @Test
    @DisplayName("Should sum study time minutes from all scheduled items")
    void testStudyTimeSummation() {
        when(practiceService.getTodayQuotaStatus(any(User.class))).thenReturn(
                new TodayQuotaResponse(LocalDate.now(), 5, 0, 5, 5, 0, 5, 5, 0, 5)
        );
        when(taskService.getUserTasks(any(User.class), isNull())).thenReturn(List.of());
        when(topicService.getTopicsDueForRevision(any(User.class))).thenReturn(List.of());

        // 3 items: 45 + 60 + 30 = 135 minutes
        when(schedulerService.generateTodaySchedule(any(User.class))).thenReturn(
                new DailyScheduleResponse(
                        LocalDate.now(), DayOfWeek.MONDAY, List.of(),
                        List.of(
                                new ScheduledSlotItem(LocalTime.of(8, 0), LocalTime.of(8, 45), 45,
                                        ScheduledItemType.DSA_PRACTICE, "DSA", "Arrays", null),
                                new ScheduledSlotItem(LocalTime.of(9, 0), LocalTime.of(10, 0), 60,
                                        ScheduledItemType.TOPIC_REVISION, "Revision", "OS", null),
                                new ScheduledSlotItem(LocalTime.of(10, 0), LocalTime.of(10, 30), 30,
                                        ScheduledItemType.SQL_PRACTICE, "SQL", "Joins", null)
                        ),
                        List.of(), false, ""
                )
        );

        DashboardMetricsResponse metrics = dashboardService.getMetrics(testUser);

        assertEquals(135, metrics.studyTimeMinutes());
        assertEquals(3, metrics.scheduledItemsCount());
    }
}
