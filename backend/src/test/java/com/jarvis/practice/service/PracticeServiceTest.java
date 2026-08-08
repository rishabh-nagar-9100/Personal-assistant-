package com.jarvis.practice.service;

import com.jarvis.auth.model.User;
import com.jarvis.practice.dto.QuotaConfigRequest;
import com.jarvis.practice.dto.QuotaConfigResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.model.DailyProgress;
import com.jarvis.practice.model.DailyQuotaConfig;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.repository.DailyProgressRepository;
import com.jarvis.practice.repository.DailyQuotaConfigRepository;
import com.jarvis.practice.repository.PracticeQuestionRepository;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private PracticeQuestionRepository practiceRepository;

    @Mock
    private DailyQuotaConfigRepository quotaConfigRepository;

    @Mock
    private DailyProgressRepository progressRepository;

    @Mock
    private com.jarvis.auth.repository.UserRepository userRepository;

    private SpacedRepetitionCalculator calculator;
    private PracticeService practiceService;
    private User testUser;

    @BeforeEach
    void setUp() {
        calculator = new SpacedRepetitionCalculator();
        practiceService = new PracticeService(practiceRepository, quotaConfigRepository, progressRepository, userRepository, calculator);
        testUser = new User(UUID.randomUUID(), "practiceuser@example.com");
    }

    @Test
    @DisplayName("Should initialize default 5/5/5 targets when quota config does not exist")
    void testGetDefaultQuotaConfig() {
        when(quotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.empty());
        when(quotaConfigRepository.save(any(DailyQuotaConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuotaConfigResponse config = practiceService.getQuotaConfig(testUser);

        assertEquals(5, config.dsaTarget());
        assertEquals(5, config.sqlTarget());
        assertEquals(5, config.aptitudeTarget());
    }

    @Test
    @DisplayName("Should update quota config targets successfully")
    void testUpdateQuotaConfig() {
        DailyQuotaConfig existingConfig = new DailyQuotaConfig(testUser, 5, 5, 5);
        when(quotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(existingConfig));
        when(quotaConfigRepository.save(any(DailyQuotaConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuotaConfigResponse updated = practiceService.updateQuotaConfig(testUser, new QuotaConfigRequest(10, 8, 6));

        assertEquals(10, updated.dsaTarget());
        assertEquals(8, updated.sqlTarget());
        assertEquals(6, updated.aptitudeTarget());
    }

    @Test
    @DisplayName("Should calculate remaining today quota counts accurately")
    void testGetTodayQuotaStatus() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyQuotaConfig config = new DailyQuotaConfig(testUser, 5, 5, 5);
        DailyProgress progress = new DailyProgress(testUser, today);
        progress.setSqlDone(3);
        progress.setDsaDone(5); // fully completed

        when(quotaConfigRepository.findById(testUser.getId())).thenReturn(Optional.of(config));
        when(progressRepository.findByUserIdAndDate(eq(testUser.getId()), eq(today))).thenReturn(Optional.of(progress));

        TodayQuotaResponse todayQuota = practiceService.getTodayQuotaStatus(testUser);

        assertEquals(5, todayQuota.dsaTarget());
        assertEquals(5, todayQuota.dsaDone());
        assertEquals(0, todayQuota.dsaRemaining()); // 0 remaining (not negative)

        assertEquals(5, todayQuota.sqlTarget());
        assertEquals(3, todayQuota.sqlDone());
        assertEquals(2, todayQuota.sqlRemaining());

        assertEquals(5, todayQuota.aptitudeTarget());
        assertEquals(0, todayQuota.aptitudeDone());
        assertEquals(5, todayQuota.aptitudeRemaining());
    }

    @Test
    @DisplayName("Should increment category progress for SQL questions")
    void testIncrementCategoryProgress() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyProgress progress = new DailyProgress(testUser, today);

        when(progressRepository.findByUserIdAndDate(eq(testUser.getId()), eq(today))).thenReturn(Optional.of(progress));
        when(progressRepository.save(any(DailyProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        practiceService.incrementCategoryProgress(testUser, PracticeCategoryType.SQL);

        assertEquals(1, progress.getSqlDone());
        verify(progressRepository, times(1)).save(progress);
    }
}
