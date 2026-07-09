package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAnalyticsDto {

    private Long totalSessions;

    private Long totalConversations;

    private Long totalPracticeDays;

    private Long currentStreak;

    private Long longestStreak;

    private Double averageScore;

    private Long reportsGenerated;

    private Map<String, Long> modeUsage;

    private Map<String, Long> difficultyUsage;

    private Map<String, Long> activityHeatmap;
}