package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.CustomPracticeQuestion;
import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomPracticeQuestionRepository
        extends JpaRepository<CustomPracticeQuestion, Long> {

    List<CustomPracticeQuestion> findAllBySessionOrderByQuestionOrderAsc(
            CustomPracticeSession session
    );

    Optional<CustomPracticeQuestion> findByIdAndSession(
            Long questionId,
            CustomPracticeSession session
    );

    Optional<CustomPracticeQuestion> findBySessionAndQuestionOrder(
            CustomPracticeSession session,
            Integer questionOrder
    );

    List<CustomPracticeQuestion>
    findAllBySessionAndRetryRequiredTrueAndRetryCompletedFalseOrderByQuestionOrderAsc(
            CustomPracticeSession session
    );

    long countBySessionAndCompletedTrue(
            CustomPracticeSession session
    );

    long countBySessionAndRetryRequiredTrueAndRetryCompletedFalse(
            CustomPracticeSession session
    );
}