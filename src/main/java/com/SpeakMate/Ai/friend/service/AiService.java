package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.SessionReportDto;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;

public interface AiService {

    String generateQuestion(String topic, SessionMode mode, DifficultyLevel difficultyLevel);

    String generateFeedback(
            String question,
            String answer,
            SessionMode mode
    );

    SessionReportDto generateSessionReport(
            String conversationHistory,
            SessionMode mode
    );

    String generateNextQuestion(String topic, String previousQuestion,
                                String userAnswer,
                                SessionMode mode, DifficultyLevel difficultyLevel
    );


}