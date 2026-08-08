package com.SpeakMate.Ai.friend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyOverviewDto {
    private int year;

    private int month;

    private long activeUsers;

    private long newUsers;

    private long totalSessions;

    private long totalConversations;
}
