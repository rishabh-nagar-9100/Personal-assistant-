package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.dto.CreateDsaQuestionRequest;
import com.jarvis.dsa.dto.DsaQuestionResponse;
import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.dsa.repository.DsaQuestionRepository;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DsaQuestionServiceTest {

    @Mock
    private DsaQuestionRepository repository;

    @Mock
    private DocumentParserService documentParserService;

    private DsaExcelParserService excelParserService;
    private SpacedRepetitionCalculator calculator;
    private DsaQuestionService dsaQuestionService;
    private User testUser;

    @BeforeEach
    void setUp() {
        excelParserService = new DsaExcelParserService();
        calculator = new SpacedRepetitionCalculator();
        dsaQuestionService = new DsaQuestionService(repository, excelParserService, documentParserService, calculator);
        testUser = new User(UUID.randomUUID(), "dsauser@example.com");
    }

    @Test
    @DisplayName("Should create single DSA question successfully")
    void testCreateQuestion() {
        CreateDsaQuestionRequest request = new CreateDsaQuestionRequest(
                "3Sum",
                "Arrays",
                DsaDifficulty.MEDIUM,
                "https://leetcode.com/problems/3sum",
                DsaStatus.NOT_STARTED
        );

        when(repository.save(any(DsaQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DsaQuestionResponse response = dsaQuestionService.createQuestion(testUser, request);

        assertNotNull(response);
        assertEquals("3Sum", response.title());
        assertEquals("Arrays", response.topic());
        assertEquals(DsaDifficulty.MEDIUM, response.difficulty());
    }

    @Test
    @DisplayName("Should review DSA question and update status to SOLVED when ReviewQuality is GOOD")
    void testReviewQuestionGood() {
        UUID questionId = UUID.randomUUID();
        DsaQuestion q = new DsaQuestion(testUser, "Reverse Linked List", "LinkedList", DsaDifficulty.EASY, null, DsaStatus.NOT_STARTED);

        when(repository.findByIdAndUserId(questionId, testUser.getId())).thenReturn(Optional.of(q));
        when(repository.save(any(DsaQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DsaQuestionResponse response = dsaQuestionService.reviewQuestion(testUser, questionId, ReviewQuality.GOOD);

        assertEquals(DsaStatus.SOLVED, response.status());
        assertEquals(2.6, response.easeFactor());
        assertEquals(1, response.intervalDays());
        assertNotNull(response.nextRevisionAt());
    }

    @Test
    @DisplayName("Should build today's question queue combining due revisions and new un-attempted questions up to quota limit")
    void testGetTodayQuestions() {
        DsaQuestion dueQuestion = new DsaQuestion(testUser, "Valid Parentheses", "Stack", DsaDifficulty.EASY, null, DsaStatus.NEEDS_REVISION);
        dueQuestion.setNextRevisionAt(Instant.now().minus(1, ChronoUnit.HOURS));

        DsaQuestion newQuestion = new DsaQuestion(testUser, "Climbing Stairs", "DP", DsaDifficulty.EASY, null, DsaStatus.NOT_STARTED);

        when(repository.findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(dueQuestion));
        when(repository.findByUserIdAndStatus(eq(testUser.getId()), eq(DsaStatus.NOT_STARTED), any(Pageable.class)))
                .thenReturn(List.of(newQuestion));

        List<DsaQuestionResponse> todayQueue = dsaQuestionService.getTodayQuestions(testUser, 2);

        assertEquals(2, todayQueue.size());
        assertEquals("Valid Parentheses", todayQueue.get(0).title());
        assertEquals("Climbing Stairs", todayQueue.get(1).title());
    }
}
