package com.jarvis.topic.service;

import com.jarvis.auth.model.User;
import com.jarvis.spacedrepetition.dto.SpacedRepetitionResult;
import com.jarvis.spacedrepetition.model.ReviewQuality;
import com.jarvis.spacedrepetition.service.SpacedRepetitionCalculator;
import com.jarvis.topic.dto.CreateTopicRequest;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.model.Subject;
import com.jarvis.topic.model.Topic;
import com.jarvis.topic.model.TopicStatus;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final SpacedRepetitionCalculator calculator;

    public TopicService(TopicRepository topicRepository,
                        SubjectRepository subjectRepository,
                        SpacedRepetitionCalculator calculator) {
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.calculator = calculator;
    }

    @Transactional
    public TopicResponse createTopic(User user, CreateTopicRequest request) {
        Subject subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + request.subjectId()));

        Topic topic = new Topic(subject, request.name());
        Topic saved = topicRepository.save(topic);
        return TopicResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsBySubject(User user, UUID subjectId) {
        // Validate user owns subject
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + subjectId));

        return topicRepository.findBySubjectIdOrderByNameAsc(subject.getId())
                .stream()
                .map(TopicResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TopicResponse reviewTopic(User user, UUID topicId, ReviewQuality quality) {
        Topic topic = topicRepository.findByIdAndSubjectUserId(topicId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with id: " + topicId));

        SpacedRepetitionResult result = calculator.calculateNextRevision(
                topic.getEaseFactor(),
                topic.getIntervalDays(),
                topic.getRepetitionCount(),
                quality
        );

        topic.setEaseFactor(result.easeFactor());
        topic.setIntervalDays(result.intervalDays());
        topic.setRepetitionCount(result.repetitionCount());
        topic.setLastStudiedAt(result.lastStudiedAt());
        topic.setNextRevisionAt(result.nextRevisionAt());
        topic.setStatus(quality == ReviewQuality.GOOD ? TopicStatus.COMPLETED : TopicStatus.IN_PROGRESS);

        Topic updated = topicRepository.save(topic);
        return TopicResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<TopicResponse> getTopicsDueForRevision(User user) {
        return topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(user.getId(), Instant.now())
                .stream()
                .map(TopicResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteTopic(User user, UUID topicId) {
        Topic topic = topicRepository.findByIdAndSubjectUserId(topicId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with id: " + topicId));
        topicRepository.delete(topic);
    }
}
