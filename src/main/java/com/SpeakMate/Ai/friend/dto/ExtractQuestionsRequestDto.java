package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractQuestionsRequestDto {

    @NotBlank(message = "Question content cannot be empty")
    private String content;
}