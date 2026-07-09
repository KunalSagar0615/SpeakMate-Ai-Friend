package com.SpeakMate.Ai.friend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionReportDto {

    private Long sessionId;

    private String overallEvaluation;

    private String strengths;

    private String areasOfImprovement;

    private String recommendations;
}