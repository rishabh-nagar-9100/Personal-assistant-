package com.jarvis.dsa.repository;

import com.jarvis.dsa.model.DsaQuestion;
import com.jarvis.dsa.model.DsaStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DsaQuestionRepository extends JpaRepository<DsaQuestion, UUID> {

    List<DsaQuestion> findByUserIdOrderByTopicAscTitleAsc(UUID userId);

    List<DsaQuestion> findByUserIdAndNextRevisionAtLessThanEqualOrderByNextRevisionAtAsc(UUID userId, Instant now);

    List<DsaQuestion> findByUserIdAndStatus(UUID userId, DsaStatus status, Pageable pageable);

    Optional<DsaQuestion> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);
}
