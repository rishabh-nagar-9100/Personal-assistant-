package com.jarvis.practice.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.practice.dto.CreatePracticeQuestionRequest;
import com.jarvis.practice.dto.PracticeQuestionResponse;
import com.jarvis.practice.dto.QuotaConfigRequest;
import com.jarvis.practice.dto.QuotaConfigResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.model.DailyProgress;
import com.jarvis.practice.model.DailyQuotaConfig;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.model.PracticeQuestion;
import com.jarvis.practice.repository.DailyProgressRepository;
import com.jarvis.practice.repository.DailyQuotaConfigRepository;
import com.jarvis.practice.repository.PracticeQuestionRepository;
import com.jarvis.spacedrepetition.dto.SpacedRepetitionResult;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class PracticeService {

    private final PracticeQuestionRepository practiceRepository;
    private final DailyQuotaConfigRepository quotaConfigRepository;
    private final DailyProgressRepository progressRepository;
    private final com.jarvis.auth.repository.UserRepository userRepository;
    private final SpacedRepetitionCalculator calculator;

    public PracticeService(PracticeQuestionRepository practiceRepository,
                           DailyQuotaConfigRepository quotaConfigRepository,
                           DailyProgressRepository progressRepository,
                           com.jarvis.auth.repository.UserRepository userRepository,
                           SpacedRepetitionCalculator calculator) {
        this.practiceRepository = practiceRepository;
        this.quotaConfigRepository = quotaConfigRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.calculator = calculator;
    }

    @Transactional
    public QuotaConfigResponse getQuotaConfig(User user) {
        DailyQuotaConfig config = getOrCreateQuotaConfig(user);
        return QuotaConfigResponse.fromEntity(config);
    }

    @Transactional
    public QuotaConfigResponse updateQuotaConfig(User user, QuotaConfigRequest request) {
        DailyQuotaConfig config = getOrCreateQuotaConfig(user);
        config.setDsaTarget(request.dsaTarget());
        config.setSqlTarget(request.sqlTarget());
        config.setAptitudeTarget(request.aptitudeTarget());
        DailyQuotaConfig updated = quotaConfigRepository.save(config);
        return QuotaConfigResponse.fromEntity(updated);
    }

    @Transactional
    public TodayQuotaResponse getTodayQuotaStatus(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyQuotaConfig config = getOrCreateQuotaConfig(user);
        DailyProgress progress = getOrCreateTodayProgress(user, today);

        int dsaRemaining = Math.max(0, config.getDsaTarget() - progress.getDsaDone());
        int sqlRemaining = Math.max(0, config.getSqlTarget() - progress.getSqlDone());
        int aptitudeRemaining = Math.max(0, config.getAptitudeTarget() - progress.getAptitudeDone());

        return new TodayQuotaResponse(
                today,
                config.getDsaTarget(),
                progress.getDsaDone(),
                dsaRemaining,
                config.getSqlTarget(),
                progress.getSqlDone(),
                sqlRemaining,
                config.getAptitudeTarget(),
                progress.getAptitudeDone(),
                aptitudeRemaining
        );
    }

    @Transactional
    public PracticeQuestionResponse createPracticeQuestion(User user, CreatePracticeQuestionRequest request) {
        PracticeQuestion question = new PracticeQuestion(
                user,
                request.categoryType(),
                request.subCategory(),
                request.title(),
                request.difficulty()
        );
        PracticeQuestion saved = practiceRepository.save(question);
        return PracticeQuestionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PracticeQuestionResponse> getPracticeQuestions(User user, PracticeCategoryType categoryType) {
        return practiceRepository.findByUserIdAndCategoryTypeOrderByTitleAsc(user.getId(), categoryType)
                .stream()
                .map(PracticeQuestionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PracticeQuestionResponse updateQuestionStatus(User user, UUID id, DsaStatus status) {
        PracticeQuestion question = practiceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Practice question not found with id: " + id));
        question.setStatus(status);
        if (status == DsaStatus.SOLVED) {
            question.setLastAttemptedAt(java.time.Instant.now());
            incrementCategoryProgress(user, question.getCategoryType());
        }
        PracticeQuestion updated = practiceRepository.save(question);
        return PracticeQuestionResponse.fromEntity(updated);
    }

    @Transactional
    public PracticeQuestionResponse reviewPracticeQuestion(User user, UUID id, ReviewQuality quality) {
        PracticeQuestion question = practiceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Practice question not found with id: " + id));

        SpacedRepetitionResult result = calculator.calculateNextRevision(
                question.getEaseFactor(),
                question.getIntervalDays(),
                question.getRepetitionCount(),
                quality
        );

        question.setEaseFactor(result.easeFactor());
        question.setIntervalDays(result.intervalDays());
        question.setRepetitionCount(result.repetitionCount());
        question.setLastAttemptedAt(result.lastStudiedAt());
        question.setNextRevisionAt(result.nextRevisionAt());
        question.setStatus(quality == ReviewQuality.GOOD ? DsaStatus.SOLVED : DsaStatus.NEEDS_REVISION);

        PracticeQuestion updated = practiceRepository.save(question);

        // Auto increment today's progress for this category
        incrementCategoryProgress(user, question.getCategoryType());

        return PracticeQuestionResponse.fromEntity(updated);
    }

    @Transactional
    public void incrementCategoryProgress(User user, PracticeCategoryType categoryType) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyProgress progress = getOrCreateTodayProgress(user, today);
        if (categoryType == PracticeCategoryType.SQL) {
            progress.setSqlDone(progress.getSqlDone() + 1);
        } else if (categoryType == PracticeCategoryType.APTITUDE) {
            progress.setAptitudeDone(progress.getAptitudeDone() + 1);
        } else if (categoryType == PracticeCategoryType.DSA) {
            progress.setDsaDone(progress.getDsaDone() + 1);
        }
        progressRepository.save(progress);
    }

    @Transactional
    public void deletePracticeQuestion(User user, UUID id) {
        PracticeQuestion question = practiceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Practice question not found with id: " + id));
        practiceRepository.delete(question);
    }

    @Transactional
    public void clearAllQuestions(User user) {
        practiceRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public DailyQuotaConfig getOrCreateQuotaConfig(User user) {
        return quotaConfigRepository.findById(user.getId())
                .orElseGet(() -> {
                    User managedUser = userRepository.findById(user.getId()).orElse(user);
                    return quotaConfigRepository.save(new DailyQuotaConfig(managedUser, 5, 5, 5));
                });
    }

    @Transactional
    public DailyProgress getOrCreateTodayProgress(User user, LocalDate date) {
        return progressRepository.findByUserIdAndDate(user.getId(), date)
                .orElseGet(() -> {
                    User managedUser = userRepository.findById(user.getId()).orElse(user);
                    return progressRepository.save(new DailyProgress(managedUser, date));
                });
    }
}
