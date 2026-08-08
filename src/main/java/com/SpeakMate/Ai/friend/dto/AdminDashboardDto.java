package com.SpeakMate.Ai.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {

    private long totalUsers;

    private long activeUsersCurrentWeek;

    private long activeUsersCurrentMonth;

    private long activeUsersLastMonth;

    private List<AdminUserListDto> users;
}
