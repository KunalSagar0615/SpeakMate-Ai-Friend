package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeReportDto {

    private Long sessionId;

    private String sessionName;

    private CustomPracticeStatus status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer correctQuestions;

    private Integer partiallyCorrectQuestions;

    private Integer incorrectQuestions;

    private Integer skippedQuestions;

    private Integer notAttemptedQuestions;

    private Integer retriedQuestions;

    private Double overallScore;

    private String overallAiFeedback;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime endedAt;

    private LocalDateTime expiredAt;

    private Long practiceDurationSeconds;

    private List<CustomPracticeQuestionReportDto> questions;
}