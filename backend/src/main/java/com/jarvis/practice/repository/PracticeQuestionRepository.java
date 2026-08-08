package com.jarvis.practice.repository;

import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.model.PracticeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PracticeQuestionRepository extends JpaRepository<PracticeQuestion, UUID> {

    List<PracticeQuestion> findByUserIdAndCategoryTypeOrderByTitleAsc(UUID userId, PracticeCategoryType categoryType);

    List<PracticeQuestion> findByUserIdOrderByTitleAsc(UUID userId);

    List<PracticeQuestion> findByUserIdAndSubjectIdOrderByTitleAsc(UUID userId, UUID subjectId);

    List<PracticeQuestion> findByUserIdAndSubjectNameIgnoreCaseOrderByTitleAsc(UUID userId, String subjectName);

    Optional<PracticeQuestion> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);
}
