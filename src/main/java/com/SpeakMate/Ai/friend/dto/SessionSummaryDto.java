package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryDto {

    private Long sessionId;
    private String topic;
    private Integer totalQuestions;
    private Integer totalScore;
    private Double averageScore;
    private Long totalConversations;
}