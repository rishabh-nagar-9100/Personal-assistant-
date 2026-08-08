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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubjectRepository subjectRepository;

    private SpacedRepetitionCalculator calculator;
    private TopicService topicService;
    private User testUser;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        calculator = new SpacedRepetitionCalculator();
        topicService = new TopicService(topicRepository, subjectRepository, calculator);
        testUser = new User(UUID.randomUUID(), "topicuser@example.com");
        testSubject = new Subject(testUser, "Computer Science");
    }

    @Test
    @DisplayName("Should create topic under valid subject")
    void testCreateTopic() {
        UUID subjectId = UUID.randomUUID();
        CreateTopicRequest request = new CreateTopicRequest(subjectId, "Binary Trees");

        when(subjectRepository.findByIdAndUserId(subjectId, testUser.getId())).thenReturn(Optional.of(testSubject));
        when(topicRepository.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TopicResponse response = topicService.createTopic(testUser, request);

        assertNotNull(response);
        assertEquals("Binary Trees", response.name());
        assertEquals(TopicStatus.NOT_STARTED, response.status());
        verify(topicRepository, times(1)).save(any(Topic.class));
    }

    @Test
    @DisplayName("Should review topic and update next revision using SM-2 calculator")
    void testReviewTopic() {
        UUID topicId = UUID.randomUUID();
        Topic topic = new Topic(testSubject, "Dynamic Programming");

        Instant now = Instant.now();
        Instant nextRev = now.plus(1, ChronoUnit.DAYS);

        when(topicRepository.findByIdAndSubjectUserId(topicId, testUser.getId())).thenReturn(Optional.of(topic));
        when(topicRepository.save(any(Topic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TopicResponse response = topicService.reviewTopic(testUser, topicId, ReviewQuality.GOOD);

        assertEquals(TopicStatus.COMPLETED, response.status());
        assertEquals(2.6, response.easeFactor());
        assertEquals(1, response.intervalDays());
        assertEquals(1, response.repetitionCount());
        assertNotNull(response.nextRevisionAt());
    }

    @Test
    @DisplayName("Should fetch topics due for revision")
    void testGetTopicsDueForRevision() {
        Topic topic = new Topic(testSubject, "Graph Traversal");
        topic.setNextRevisionAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(topicRepository.findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(eq(testUser.getId()), any(Instant.class)))
                .thenReturn(List.of(topic));

        List<TopicResponse> dueTopics = topicService.getTopicsDueForRevision(testUser);

        assertEquals(1, dueTopics.size());
        assertEquals("Graph Traversal", dueTopics.get(0).name());
    }
}
