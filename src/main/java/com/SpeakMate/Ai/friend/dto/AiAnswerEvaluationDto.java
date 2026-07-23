package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.AnswerEvaluationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnswerEvaluationDto {

    private AnswerEvaluationStatus status;

    private Integer score;

    private String feedback;
}