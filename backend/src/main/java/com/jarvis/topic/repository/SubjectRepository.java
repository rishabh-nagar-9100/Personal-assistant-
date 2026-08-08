package com.jarvis.topic.repository;

import com.jarvis.topic.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findByUserIdOrderByNameAsc(UUID userId);

    Optional<Subject> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);
}
