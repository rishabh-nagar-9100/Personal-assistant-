package com.jarvis.scheduler.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.dsa.repository.DsaQuestionRepository;
import com.jarvis.dsa.service.DsaExcelParserService;
import com.jarvis.dsa.service.DocumentParserService;
import com.jarvis.dsa.service.DsaQuestionService;
import com.jarvis.practice.model.DailyProgress;
import com.jarvis.practice.model.DailyQuotaConfig;
import com.jarvis.practice.repository.DailyProgressRepository;
import com.jarvis.practice.repository.DailyQuotaConfigRepository;
import com.jarvis.practice.repository.PracticeQuestionRepository;
import com.jarvis.practice.service.PracticeService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.dto.ScheduledSlotItem;
import com.jarvis.scheduler.model.ScheduledItemType;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import com.jarvis.task.model.PriorityEvent;
import com.jarvis.task.model.PriorityEventType;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.repository.PriorityEventRepository;
import com.jarvis.task.repository.TaskRepository;
import com.jarvis.task.service.PriorityEventService;
import com.jarvis.task.service.TaskService;
import com.jarvis.timetable.model.SlotType;
import com.jarvis.timetable.model.TimetableSlot;
import com.jarvis.timetable.model.UserDailyState;
import com.jarvis.timetable.repository.TimetableSlotRepository;
import com.jarvis.timetable.repository.UserDailyStateRepository;
import com.jarvis.timetable.service.TimetableService;
import com.jarvis.topic.model.Subject;
import com.jarvis.topic.model.Topic;
import com.jarvis.topic.model.TopicStatus;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import com.jarvis.topic.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedulerServiceTest {

    @Mock private TimetableSlotRepository timetableSlotRepository;
    @Mock private UserDailyStateRepository userDailyStateRepository;
    @Mock private PriorityEventRepository priorityEventRepository;
    @Mock private TopicRepository topicRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private DsaQuestionRepository dsaQuestionRepository;
    @Mock private DailyQuotaConfigRepository dailyQuotaConfigRepository;
    @Mock private DailyProgressRepository dailyProgressRepository;
    @Mock private PracticeQuestionRepository practiceQuestionRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private com.jarvis.auth.repository.UserRepository userRepository;

    private SchedulerService schedulerService;
    private User testUser;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        SpacedRepetitionCalculator calculator = new SpacedRepetitionCalculator();
        DsaExcelParserService excelParser = new DsaExcelParserService();

        TimetableService timetableService = new TimetableService(timetableSlotRepository, userDailyStateRepository);
        PriorityEventService priorityEventService = new PriorityEventService(priorityEventRepository);
        TopicService topicService = new TopicService(topicRepository, subjectRepository, calculator);
        DocumentParserService docParser = mock(DocumentParserService.class);
        DsaQuestionService dsaQuestionService = new DsaQuestionService(dsaQuestionRepository, excelParser, docParser, calculator);
        PracticeService practiceService = new PracticeService(practiceQuestionRepository, dailyQuotaConfigRepository, dailyProgressRepository, userRepository, calculator);
        TaskService taskService = new TaskService(taskRepository);

        schedulerService = new SchedulerService(
                timetableService,
                priorityEventService,
                topicService,
                dsaQuestionService,
                practiceService,
                taskService
        );

        testUser = new User(UUID.randomUUID(), "scheduleruser@example.com");
        testDate = LocalDate.now(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Normal Day: Free slots fit overdue items and quota questions smoothly without overflow")
    void testNormalDayScheduling() {
        when(userDailyStateRepository.findByUserIdAndDate(eq(testUser.getId()), any()))
                .thenReturn(Optional.of(new UserDailyState(testUser, testDate, "DAY_1")));

        // Free slots: 08:00 - 12:00 (240 mins) -> 1 class slot 12:00-14:00 leaves 08:00-12:00 free
        TimetableSlot classSlot = new TimetableSlot(testUser, testDate.getDayOfWeek(), "DAY_1", LocalTime.of(12, 0), LocalTime.of(14, 0), SlotType.CLASS, "Physics");
        when(timetableSlotRepository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(testUser.getId(), testDate.getDayOfWeek()))
                .thenReturn(List.of(classSlot));
        when(timetableSlotRepository.findByUserIdAndDayOrderOrderByStartTimeAsc(eq(testUser.getId()), any()))
                .thenReturn(List.of(classSlot));

        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // 1 overdue topic
        Subject subject = new Subject(testUser, "Math");
        Topic topic = new Topic(subject, "Calculus");
        topic.setStatus(TopicStatus.IN_PROGRESS);
        topic.setNextRevisionAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(topic));

        // Quotas: 5/5/5 targets, 4/5/5 done -> 1 DSA remaining
        DailyQuotaConfig quotaConfig = new DailyQuotaConfig(testUser, 5, 5, 5);
        DailyProgress progress = new DailyProgress(testUser, testDate);
        progress.setDsaDone(4);
        progress.setSqlDone(5);
        progress.setAptitudeDone(5);
        when(dailyQuotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(quotaConfig));
        when(dailyProgressRepository.findByUserIdAndDate(testUser.getId(), testDate)).thenReturn(Optional.of(progress));

        DsaQuestion dsa = new DsaQuestion(testUser, "Two Sum", "Arrays", DsaDifficulty.EASY, null, DsaStatus.NOT_STARTED);
        when(dsaQuestionRepository.findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(Collections.emptyList());
        when(dsaQuestionRepository.findByUserIdAndStatus(eq(testUser.getId()), eq(DsaStatus.NOT_STARTED), any(Pageable.class)))
                .thenReturn(List.of(dsa));

        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(testUser.getId(), TaskStatus.PENDING))
                .thenReturn(Collections.emptyList());

        DailyScheduleResponse response = schedulerService.generateScheduleForDate(testUser, testDate);

        assertNotNull(response);
        // 1 college class + 1 topic revision + 1 DSA practice = 3 scheduled items
        assertEquals(3, response.scheduledItems().size());
        assertEquals(0, response.overflowItems().size());

        // Item 1: Topic Revision (08:00 - 08:30)
        assertEquals(ScheduledItemType.TOPIC_REVISION, response.scheduledItems().get(0).itemType());
        assertEquals(LocalTime.of(8, 0), response.scheduledItems().get(0).startTime());
        assertEquals(LocalTime.of(8, 30), response.scheduledItems().get(0).endTime());

        // Item 2: DSA Practice (08:30 - 09:00)
        assertEquals(ScheduledItemType.DSA_PRACTICE, response.scheduledItems().get(1).itemType());
        assertEquals(LocalTime.of(8, 30), response.scheduledItems().get(1).startTime());
        assertEquals(LocalTime.of(9, 0), response.scheduledItems().get(1).endTime());
    }

    @Test
    @DisplayName("Priority Event Day: Generates schedule with exam prep boosted topic item first")
    void testPriorityEventDayScheduling() {
        when(userDailyStateRepository.findByUserIdAndDate(eq(testUser.getId()), any()))
                .thenReturn(Optional.of(new UserDailyState(testUser, testDate, "DAY_1")));

        TimetableSlot classSlot = new TimetableSlot(testUser, testDate.getDayOfWeek(), "DAY_1", LocalTime.of(12, 0), LocalTime.of(14, 0), SlotType.CLASS, "Physics");
        when(timetableSlotRepository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(testUser.getId(), testDate.getDayOfWeek()))
                .thenReturn(List.of(classSlot));
        when(timetableSlotRepository.findByUserIdAndDayOrderOrderByStartTimeAsc(eq(testUser.getId()), any()))
                .thenReturn(List.of(classSlot));

        PriorityEvent event = new PriorityEvent(testUser, "Google Placement", Instant.now().plus(2, ChronoUnit.DAYS), PriorityEventType.PLACEMENT_TEST, "JD", List.of("Trees"));
        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(event));

        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(Collections.emptyList());
        when(dailyQuotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(new DailyQuotaConfig(testUser, 5, 5, 5)));
        DailyProgress progress = new DailyProgress(testUser, testDate);
        progress.setDsaDone(5);
        progress.setSqlDone(5);
        progress.setAptitudeDone(5);
        when(dailyProgressRepository.findByUserIdAndDate(testUser.getId(), testDate)).thenReturn(Optional.of(progress));
        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(testUser.getId(), TaskStatus.PENDING))
                .thenReturn(Collections.emptyList());

        DailyScheduleResponse response = schedulerService.generateScheduleForDate(testUser, testDate);

        assertTrue(response.hasPriorityEvents());
        assertFalse(response.scheduledItems().isEmpty());
        ScheduledSlotItem prepItem = response.scheduledItems().stream()
                .filter(i -> i.itemType() == ScheduledItemType.PRIORITY_EVENT_PREP)
                .findFirst().orElseThrow();
        assertEquals(LocalTime.of(8, 0), prepItem.startTime());
        assertEquals(LocalTime.of(9, 0), prepItem.endTime()); // 60 mins prep
    }

    @Test
    @DisplayName("Overloaded Day: More due items than available free time populates overflow items")
    void testOverloadedDayOverflow() {
        when(userDailyStateRepository.findByUserIdAndDate(eq(testUser.getId()), any()))
                .thenReturn(Optional.of(new UserDailyState(testUser, testDate, "DAY_1")));

        // Free slots: only 30 mins available (08:00 to 08:30)
        TimetableSlot classSlot = new TimetableSlot(testUser, testDate.getDayOfWeek(), "DAY_1", LocalTime.of(8, 30), LocalTime.of(22, 0), SlotType.CLASS, "Full Day Class");
        when(timetableSlotRepository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(eq(testUser.getId()), any()))
                .thenReturn(List.of(classSlot));
        when(timetableSlotRepository.findByUserIdAndDayOrderOrderByStartTimeAsc(eq(testUser.getId()), any()))
                .thenReturn(List.of(classSlot));

        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(Collections.emptyList());

        // 2 due topics (30 mins each -> only 1 topic fits into 30 min window, 1 overflows)
        Subject subject = new Subject(testUser, "CS");
        Topic topic1 = new Topic(subject, "Topic 1");
        Topic topic2 = new Topic(subject, "Topic 2");
        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(topic1, topic2));

        when(dailyQuotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(new DailyQuotaConfig(testUser, 5, 5, 5)));
        DailyProgress progress = new DailyProgress(testUser, testDate);
        progress.setDsaDone(5);
        progress.setSqlDone(5);
        progress.setAptitudeDone(5);
        when(dailyProgressRepository.findByUserIdAndDate(testUser.getId(), testDate)).thenReturn(Optional.of(progress));
        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(testUser.getId(), TaskStatus.PENDING))
                .thenReturn(Collections.emptyList());

        DailyScheduleResponse response = schedulerService.generateScheduleForDate(testUser, testDate);

        // 1 college class + 1 study topic = 2 scheduled items, and 1 topic overflows
        assertEquals(2, response.scheduledItems().size());
        assertEquals(1, response.overflowItems().size());

        assertEquals("Revise Topic: Topic 2", response.overflowItems().get(0).title());
        assertEquals(testDate.plusDays(1), response.overflowItems().get(0).suggestedCarryOverDate());
    }
}
