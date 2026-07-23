package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeSessionDto {

    private Long sessionId;

    private String sessionName;

    private CustomPracticeStatus status;

    private Integer totalQuestions;

    private Integer currentRound;

    private Integer answeredQuestions;

    private Integer remainingQuestions;

    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;

    private LocalDateTime expiresAt;

    private CustomPracticeQuestionDto currentQuestion;
}