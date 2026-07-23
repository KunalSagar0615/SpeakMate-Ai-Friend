package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.AiAnswerEvaluationDto;
import com.SpeakMate.Ai.friend.dto.AiQuestionExtractionDto;
import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;

public interface AiService {

    // =========================
    // Existing SpeakMate AI
    // =========================

    String generateQuestion(
            String topic,
            SessionMode mode,
            DifficultyLevel difficultyLevel
    );

    String generateFeedback(
            String question,
            String answer,
            SessionMode mode
    );

    SessionReportDto generateSessionReport(
            String conversationHistory,
            SessionMode mode
    );

    String generateNextQuestion(
            String topic,
            String previousQuestions,
            String userAnswer,
            SessionMode mode,
            DifficultyLevel difficultyLevel
    );

    String generateSuggestedAnswer(
            String question,
            SessionMode mode,
            DifficultyLevel difficultyLevel
    );


    // =========================
    // Custom Practice AI
    // =========================

    AiQuestionExtractionDto extractCustomPracticeQuestions(
            String content
    );

    AiAnswerEvaluationDto evaluateCustomPracticeAnswer(
            String question,
            String answer
    );

    String generateCustomPracticeOverallFeedback(
            String practiceSummary
    );
}