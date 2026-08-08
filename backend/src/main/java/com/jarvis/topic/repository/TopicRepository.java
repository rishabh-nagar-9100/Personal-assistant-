package com.jarvis.topic.repository;

import com.jarvis.topic.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findBySubjectIdOrderByNameAsc(UUID subjectId);

    List<Topic> findBySubjectUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(UUID userId, Instant now);

    Optional<Topic> findByIdAndSubjectUserId(UUID id, UUID userId);

    void deleteBySubjectId(UUID subjectId);
}
