package com.jarvis.dsa.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.dto.CreateDsaQuestionRequest;
import com.jarvis.dsa.dto.DsaQuestionResponse;
import com.jarvis.dsa.dto.ExcelImportResponse;
import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import com.jarvis.dsa.repository.DsaQuestionRepository;
import com.jarvis.spacedrepetition.dto.SpacedRepetitionResult;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DsaQuestionService {

    private final DsaQuestionRepository repository;
    private final DsaExcelParserService excelParserService;
    private final DocumentParserService documentParserService;
    private final SpacedRepetitionCalculator calculator;

    public DsaQuestionService(DsaQuestionRepository repository,
                              DsaExcelParserService excelParserService,
                              DocumentParserService documentParserService,
                              SpacedRepetitionCalculator calculator) {
        this.repository = repository;
        this.excelParserService = excelParserService;
        this.documentParserService = documentParserService;
        this.calculator = calculator;
    }

    @Transactional
    public ExcelImportResponse importExcelSheet(User user, MultipartFile file) throws Exception {
        return documentParserService.parseAndImportDocument(user, file, null, null);
    }

    @Transactional
    public ExcelImportResponse importExcelSheet(User user, MultipartFile file, String subjectName, String categoryType) throws Exception {
        return documentParserService.parseAndImportDocument(user, file, subjectName, categoryType);
    }

    @Transactional
    public DsaQuestionResponse createQuestion(User user, CreateDsaQuestionRequest request) {
        DsaQuestion question = new DsaQuestion(
                user,
                request.title(),
                request.topic(),
                request.difficulty(),
                request.sourceLink(),
                request.status()
        );
        DsaQuestion saved = repository.save(question);
        return DsaQuestionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DsaQuestionResponse> getUserQuestions(User user) {
        return repository.findByUserIdOrderByTopicAscTitleAsc(user.getId())
                .stream()
                .map(DsaQuestionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public DsaQuestionResponse reviewQuestion(User user, UUID id, ReviewQuality quality) {
        DsaQuestion question = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("DSA Question not found with id: " + id));

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

        DsaQuestion updated = repository.save(question);
        return DsaQuestionResponse.fromEntity(updated);
    }

    /**
     * Gets today's DSA question queue:
     * 1. Questions due for revision (next_revision_at <= now)
     * 2. Plus new un-attempted questions (status = NOT_STARTED) rotated day-by-day across topics.
     */
    @Transactional(readOnly = true)
    public List<DsaQuestionResponse> getTodayQuestions(User user, int limit) {
        List<DsaQuestion> dueRevisions = repository.findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(user.getId(), Instant.now());
        List<DsaQuestion> todayQueue = new ArrayList<>(dueRevisions);

        if (todayQueue.size() < limit) {
            int neededNew = limit - todayQueue.size();
            List<DsaQuestion> unstarted = repository.findByUserIdAndStatus(user.getId(), DsaStatus.NOT_STARTED, PageRequest.of(0, 100));

            if (!unstarted.isEmpty()) {
                // Group unstarted by topic
                java.util.Map<String, List<DsaQuestion>> byTopic = new java.util.LinkedHashMap<>();
                for (DsaQuestion q : unstarted) {
                    byTopic.computeIfAbsent(q.getTopic(), k -> new ArrayList<>()).add(q);
                }

                List<String> topicKeys = new ArrayList<>(byTopic.keySet());
                int dayOfYear = java.time.LocalDate.now(java.time.ZoneOffset.UTC).getDayOfYear();
                int topicOffset = dayOfYear % Math.max(1, topicKeys.size());

                // Pick questions starting from today's rotated topic offset
                int added = 0;
                int round = 0;
                while (added < neededNew && !unstarted.isEmpty()) {
                    boolean itemAddedInRound = false;
                    for (int i = 0; i < topicKeys.size(); i++) {
                        int idx = (topicOffset + i) % topicKeys.size();
                        List<DsaQuestion> qList = byTopic.get(topicKeys.get(idx));
                        if (round < qList.size()) {
                            todayQueue.add(qList.get(round));
                            added++;
                            itemAddedInRound = true;
                            if (added >= neededNew) break;
                        }
                    }
                    if (!itemAddedInRound) break;
                    round++;
                }
            }
        }

        return todayQueue.stream()
                .limit(limit)
                .map(DsaQuestionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteQuestion(User user, UUID id) {
        DsaQuestion question = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("DSA Question not found with id: " + id));
        repository.delete(question);
    }

    @Transactional
    public void clearAllQuestions(User user) {
        repository.deleteByUserId(user.getId());
    }
}
