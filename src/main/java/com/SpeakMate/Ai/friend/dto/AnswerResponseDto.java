package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponseDto {

    private String feedback;

    private Long nextConversationId;

    private String nextQuestion;
}