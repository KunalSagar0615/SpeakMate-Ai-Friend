package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionResponseDto {

    private Long sessionId;

    private CustomPracticeStatus sessionStatus;

    private Integer currentRound;

    private Boolean roundChanged;

    private Boolean practiceCompleted;

    private CustomPracticeQuestionDto nextQuestion;

    private String message;
}