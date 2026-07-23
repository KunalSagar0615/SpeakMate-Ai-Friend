package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomPracticeRequestDto {

    @NotBlank(message = "Session name cannot be empty")
    @Size(max = 150, message = "Session name cannot exceed 150 characters")
    private String sessionName;

    @NotEmpty(message = "At least one question is required")
    @Size(
            min = 1,
            max = 100,
            message = "A practice session must contain between 1 and 100 questions"
    )
    private List<
            @NotBlank(message = "Question cannot be empty")
                    String
            > questions;
}