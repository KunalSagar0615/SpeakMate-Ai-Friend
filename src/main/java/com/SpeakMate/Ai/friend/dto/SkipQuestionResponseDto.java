package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkipQuestionResponseDto {

    private Long questionId;

    private Integer questionNumber;

    private Integer currentRound;

    private Boolean retryRequired;

    private Boolean permanentlySkipped;

    private String message;
}