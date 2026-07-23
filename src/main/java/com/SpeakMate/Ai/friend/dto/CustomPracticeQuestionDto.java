package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeQuestionDto {

    private Long questionId;

    private String questionText;

    private Integer questionNumber;

    private Integer totalQuestions;

    private Integer currentRound;

    private Boolean retryQuestion;

    private String draftAnswer;
}