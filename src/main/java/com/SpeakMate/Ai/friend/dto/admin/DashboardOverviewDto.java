package com.SpeakMate.Ai.friend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDto {

    private long totalUsers;

    private long totalSessions;

    private long totalConversations;
}