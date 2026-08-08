package com.SpeakMate.Ai.friend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyOverviewDto {

    private LocalDate date;

    private long activeUsers;

    private long newUsers;

    private long totalSessions;

    private long totalConversations;
}
