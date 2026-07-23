package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.*;

import java.util.List;

public interface CustomPracticeService {

    ExtractQuestionsResponseDto extractQuestions(
            ExtractQuestionsRequestDto request
    );

    CustomPracticeSessionDto createSession(
            CreateCustomPracticeRequestDto request
    );

    AnswerEvaluationResponseDto submitAnswer(
            Long sessionId,
            SubmitCustomAnswerRequestDto request
    );

    SkipQuestionResponseDto skipCurrentQuestion(
            Long sessionId
    );

    NextQuestionResponseDto getNextQuestion(
            Long sessionId
    );

    void saveDraftAnswer(
            Long sessionId,
            SaveDraftAnswerRequestDto request
    );

    CustomPracticeSessionDto pauseSession(
            Long sessionId,
            PauseCustomPracticeRequestDto request
    );

    CustomPracticeSessionDto pauseSessionWithDefaultDuration(
            Long sessionId
    );

    CustomPracticeSessionDto resumeSession(
            Long sessionId
    );

    EndCustomPracticeResponseDto endSession(
            Long sessionId
    );

    List<CustomPracticeSummaryDto> getPausedSessions();

    List<CustomPracticeSummaryDto> getSessionHistory();

    CustomPracticeSessionDto getSession(
            Long sessionId
    );
}