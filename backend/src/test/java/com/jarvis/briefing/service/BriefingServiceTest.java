package com.jarvis.briefing.service;

import com.jarvis.auth.model.User;
import com.jarvis.briefing.client.LlmClient;
import com.jarvis.briefing.dto.DailyBriefingResponse;
import com.jarvis.briefing.model.DailyBriefing;
import com.jarvis.briefing.repository.DailyBriefingRepository;
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
import com.jarvis.scheduler.service.SchedulerService;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import com.jarvis.task.repository.PriorityEventRepository;
import com.jarvis.task.repository.TaskRepository;
import com.jarvis.task.service.PriorityEventService;
import com.jarvis.task.service.TaskService;
import com.jarvis.timetable.repository.TimetableSlotRepository;
import com.jarvis.timetable.repository.UserDailyStateRepository;
import com.jarvis.timetable.service.TimetableService;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import com.jarvis.topic.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BriefingServiceTest {

    @Mock private DailyBriefingRepository briefingRepository;
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
    @Mock private LlmClient llmClient;

    private BriefingService briefingService;
    private User testUser;
    private LocalDate today;

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

        SchedulerService schedulerService = new SchedulerService(
                timetableService,
                priorityEventService,
                topicService,
                dsaQuestionService,
                practiceService,
                taskService
        );

        briefingService = new BriefingService(briefingRepository, userRepository, schedulerService, llmClient);
        testUser = new User(UUID.randomUUID(), "briefinguser@example.com");
        today = LocalDate.now(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Uncached Call: Generates briefing via LLM client and saves to DB (isCached: false)")
    void testUncachedBriefingGeneration() {
        when(briefingRepository.findByUserIdAndDate(testUser.getId(), today)).thenReturn(Optional.empty());
        when(timetableSlotRepository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        DailyQuotaConfig quotaConfig = new DailyQuotaConfig(testUser, 5, 5, 5);
        when(dailyQuotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(quotaConfig));
        DailyProgress progress = new DailyProgress(testUser, today);
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUser.getId()), eq(today))).thenReturn(Optional.of(progress));
        when(dsaQuestionRepository.findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(dsaQuestionRepository.findByUserIdAndStatus(eq(testUser.getId()), any(), any())).thenReturn(Collections.emptyList());
        when(practiceQuestionRepository.findByUserIdAndCategoryTypeOrderByTitleAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());

        when(llmClient.generateBriefingText(anyString(), anyString())).thenReturn("Inspiring AI Briefing text");
        when(briefingRepository.save(any(DailyBriefing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyBriefingResponse response = briefingService.getTodayBriefing(testUser);

        assertNotNull(response);
        assertFalse(response.isCached());
        assertEquals("Inspiring AI Briefing text", response.briefingText());
        verify(llmClient, times(1)).generateBriefingText(anyString(), anyString());
        verify(briefingRepository, times(1)).save(any(DailyBriefing.class));
    }

    @Test
    @DisplayName("Cached Call: Serves briefing directly from DB (isCached: true, 0 LLM calls)")
    void testCachedBriefingRetrieval() {
        DailyBriefing cachedBriefing = new DailyBriefing(testUser, today, "Pre-existing Cached Briefing");
        when(briefingRepository.findByUserIdAndDate(testUser.getId(), today)).thenReturn(Optional.of(cachedBriefing));

        DailyBriefingResponse response = briefingService.getTodayBriefing(testUser);

        assertNotNull(response);
        assertTrue(response.isCached());
        assertEquals("Pre-existing Cached Briefing", response.briefingText());
        verifyNoInteractions(llmClient);
        verify(briefingRepository, never()).save(any(DailyBriefing.class));
    }

    @Test
    @DisplayName("LLM Failure / Missing Key: Falls back cleanly to template briefing without throwing exception")
    void testFallbackOnLlmFailure() {
        when(briefingRepository.findByUserIdAndDate(testUser.getId(), today)).thenReturn(Optional.empty());
        when(timetableSlotRepository.findByUserIdAndDayOfWeekOrderByStartTimeAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(priorityEventRepository.findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        DailyQuotaConfig quotaConfig = new DailyQuotaConfig(testUser, 5, 5, 5);
        when(dailyQuotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(quotaConfig));
        DailyProgress progress = new DailyProgress(testUser, today);
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUser.getId()), eq(today))).thenReturn(Optional.of(progress));
        when(dsaQuestionRepository.findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(dsaQuestionRepository.findByUserIdAndStatus(eq(testUser.getId()), any(), any())).thenReturn(Collections.emptyList());
        when(practiceQuestionRepository.findByUserIdAndCategoryTypeOrderByTitleAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());
        when(taskRepository.findByUserIdAndStatusOrderByDueDateAsc(eq(testUser.getId()), any())).thenReturn(Collections.emptyList());

        when(llmClient.generateBriefingText(anyString(), anyString())).thenThrow(new LlmClient.LlmClientException("API key missing"));
        when(briefingRepository.save(any(DailyBriefing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyBriefingResponse response = briefingService.getTodayBriefing(testUser);

        assertNotNull(response);
        assertFalse(response.isCached());
        assertTrue(response.briefingText().contains("Good morning! Here is your daily briefing"));
        verify(briefingRepository, times(1)).save(any(DailyBriefing.class));
    }
}
