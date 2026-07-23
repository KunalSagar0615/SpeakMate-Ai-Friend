package com.SpeakMate.Ai.friend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PauseCustomPracticeRequestDto {

    @NotNull(message = "Pause duration is required")
    @Min(value = 1, message = "Pause duration must be at least 1 day")
    @Max(value = 30, message = "Pause duration cannot exceed 30 days")
    private Integer pauseDays;
}