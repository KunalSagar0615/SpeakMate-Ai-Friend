package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDraftAnswerRequestDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    private String draftAnswer;
}