package com.SpeakMate.Ai.friend.dto;

import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import com.SpeakMate.Ai.friend.enumeration.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    private Long id;

    @NotBlank(message = "Topic is required")
    private String topic;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Status is required")
    private SessionStatus status;

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotNull
    private SessionMode mode;

    @NotNull(message = "Difficulty Level is required")
    private DifficultyLevel difficultyLevel;
}