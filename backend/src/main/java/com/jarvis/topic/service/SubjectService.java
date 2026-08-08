package com.jarvis.topic.service;

import com.jarvis.auth.model.User;
import com.jarvis.topic.dto.CreateSubjectRequest;
import com.jarvis.topic.dto.SubjectResponse;
import com.jarvis.topic.model.Subject;
import com.jarvis.topic.model.Topic;
import com.jarvis.topic.repository.SubjectRepository;
import com.jarvis.topic.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final com.jarvis.practice.repository.PracticeQuestionRepository practiceQuestionRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          TopicRepository topicRepository,
                          com.jarvis.practice.repository.PracticeQuestionRepository practiceQuestionRepository) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.practiceQuestionRepository = practiceQuestionRepository;
    }

    @Transactional
    public SubjectResponse createSubject(User user, CreateSubjectRequest request) {
        Subject subject = new Subject(user, request.name());
        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getUserSubjects(User user) {
        return subjectRepository.findByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(SubjectResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<com.jarvis.topic.dto.SubjectSummaryResponse> getSubjectSummaries(User user) {
        List<Subject> subjects = subjectRepository.findByUserIdOrderByNameAsc(user.getId());
        List<com.jarvis.practice.model.PracticeQuestion> allQuestions = practiceQuestionRepository.findByUserIdOrderByTitleAsc(user.getId());

        return subjects.stream().map(subject -> {
            int topicCount = topicRepository.findBySubjectIdOrderByNameAsc(subject.getId()).size();

            List<com.jarvis.practice.model.PracticeQuestion> subQuestions = allQuestions.stream()
                    .filter(q -> (q.getSubject() != null && q.getSubject().getId().equals(subject.getId()))
                            || (q.getSubjectName() != null && q.getSubjectName().equalsIgnoreCase(subject.getName())))
                    .toList();

            int total = subQuestions.size();
            int solved = (int) subQuestions.stream().filter(q -> q.getStatus() == com.jarvis.dsa.model.DsaStatus.SOLVED).count();
            int inProgress = (int) subQuestions.stream().filter(q -> q.getStatus() == com.jarvis.dsa.model.DsaStatus.IN_PROGRESS).count();
            int needsRevision = (int) subQuestions.stream().filter(q -> q.getStatus() == com.jarvis.dsa.model.DsaStatus.NEEDS_REVISION).count();
            int notStarted = (int) subQuestions.stream().filter(q -> q.getStatus() == com.jarvis.dsa.model.DsaStatus.NOT_STARTED).count();

            return new com.jarvis.topic.dto.SubjectSummaryResponse(
                    subject.getId(),
                    subject.getName(),
                    topicCount,
                    total,
                    solved,
                    inProgress,
                    needsRevision,
                    notStarted
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<com.jarvis.practice.dto.SubjectQuestionResponse> getSubjectQuestions(User user, UUID subjectId) {
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + subjectId));

        List<com.jarvis.practice.model.PracticeQuestion> questions = practiceQuestionRepository.findByUserIdOrderByTitleAsc(user.getId())
                .stream()
                .filter(q -> (q.getSubject() != null && q.getSubject().getId().equals(subjectId))
                        || (q.getSubjectName() != null && q.getSubjectName().equalsIgnoreCase(subject.getName())))
                .toList();

        return questions.stream().map(q -> new com.jarvis.practice.dto.SubjectQuestionResponse(
                q.getId(),
                subject.getId(),
                subject.getName(),
                q.getSubCategory() != null ? q.getSubCategory() : "General",
                q.getTitle(),
                q.getProblemNumber(),
                q.getDifficulty(),
                q.getStatus(),
                q.getSourceLink(),
                q.getLastAttemptedAt(),
                q.getNextRevisionAt(),
                q.getEaseFactor(),
                q.getRepetitionCount()
        )).toList();
    }

    @Transactional
    public void deleteSubject(User user, UUID subjectId) {
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + subjectId));

        // Delete all practice questions linked to this subject
        List<com.jarvis.practice.model.PracticeQuestion> subQuestions = practiceQuestionRepository.findByUserIdOrderByTitleAsc(user.getId())
                .stream()
                .filter(q -> (q.getSubject() != null && q.getSubject().getId().equals(subjectId))
                        || (q.getSubjectName() != null && q.getSubjectName().equalsIgnoreCase(subject.getName())))
                .toList();
        practiceQuestionRepository.deleteAll(subQuestions);

        List<Topic> topics = topicRepository.findBySubjectIdOrderByNameAsc(subjectId);
        topicRepository.deleteAll(topics);
        subjectRepository.delete(subject);
    }

    @Transactional
    public void clearAllUserData(User user) {
        practiceQuestionRepository.deleteByUserId(user.getId());
        List<Subject> subjects = subjectRepository.findByUserIdOrderByNameAsc(user.getId());
        for (Subject s : subjects) {
            List<Topic> topics = topicRepository.findBySubjectIdOrderByNameAsc(s.getId());
            topicRepository.deleteAll(topics);
        }
        subjectRepository.deleteAll(subjects);
    }
}
