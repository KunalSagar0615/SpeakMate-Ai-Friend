package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndCustomPracticeResponseDto {

    private Long sessionId;

    private CustomPracticeStatus status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer skippedQuestions;

    private Integer notAttemptedQuestions;

    private LocalDateTime endedAt;

    private Boolean reportAvailable;

    private String message;
}