package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequestDto {

    @NotNull(message = "Conversation Id is required")
    private Long conversationId;

    @NotBlank(message = "Answer is required")
    private String answer;
}