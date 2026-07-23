package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeSummaryDto {

    private Long sessionId;

    private String sessionName;

    private CustomPracticeStatus status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer remainingQuestions;

    private Integer currentRound;

    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;

    private LocalDateTime expiresAt;
}