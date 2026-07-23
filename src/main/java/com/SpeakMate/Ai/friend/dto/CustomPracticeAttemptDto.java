package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import com.SpeakMate.Ai.friend.enumeration.PracticeAttemptType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeAttemptDto {

    private Long attemptId;

    private Integer attemptNumber;

    private PracticeAttemptType attemptType;

    private String userAnswer;

    private AnswerEvaluationStatus status;

    private Integer score;

    private String feedback;

    private LocalDateTime submittedAt;
}