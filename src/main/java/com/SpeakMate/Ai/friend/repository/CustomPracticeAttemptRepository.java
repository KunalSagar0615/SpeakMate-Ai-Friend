package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.CustomPracticeAttempt;
import com.SpeakMate.Ai.friend.entities.CustomPracticeQuestion;
import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import com.SpeakMate.Ai.friend.enumeration.PracticeAttemptType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomPracticeAttemptRepository
        extends JpaRepository<CustomPracticeAttempt, Long> {

    List<CustomPracticeAttempt> findAllByQuestionOrderByAttemptNumberAsc(
            CustomPracticeQuestion question
    );

    Optional<CustomPracticeAttempt> findByQuestionAndAttemptNumber(
            CustomPracticeQuestion question,
            Integer attemptNumber
    );

    Optional<CustomPracticeAttempt> findByQuestionAndAttemptType(
            CustomPracticeQuestion question,
            PracticeAttemptType attemptType
    );

    boolean existsByQuestionAndAttemptNumber(
            CustomPracticeQuestion question,
            Integer attemptNumber
    );

    long countByQuestionAndEvaluationStatus(
            CustomPracticeQuestion question,
            AnswerEvaluationStatus evaluationStatus
    );
}