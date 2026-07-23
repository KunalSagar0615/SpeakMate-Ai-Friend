package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPracticeQuestionReportDto {

    private Long questionId;

    private Integer questionNumber;

    private String questionText;

    private Boolean answered;

    private Boolean skipped;

    private Boolean notAttempted;

    private Boolean retried;

    private Integer scoreUsedForOverall;

    private List<CustomPracticeAttemptDto> attempts;
}