package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private Long id;

    @NotBlank(message = "AI Question is required")
    private String aiQuestion;

    @NotBlank(message = "User Answer is required")
    private String userAnswer;

    private String aiFeedback;

    private Integer score;

    @NotNull(message = "Session Id is required")
    private Long sessionId;
}
