package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.AdminDashboardDto;
import com.SpeakMate.Ai.friend.dto.AdminUserDetailsDto;
import com.SpeakMate.Ai.friend.dto.AdminUserListDto;
import com.SpeakMate.Ai.friend.dto.admin.DailyOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.DashboardOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.MonthlyOverviewDto;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {


        DashboardOverviewDto getOverview();

        MonthlyOverviewDto getMonthlyOverview(int year, int month);

        DailyOverviewDto getDailyOverview(LocalDate date);

        List<AdminUserListDto> getAllUsers();

        AdminUserDetailsDto getUser(Long userId);


}
