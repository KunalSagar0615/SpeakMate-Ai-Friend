package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.AdminUserDetailsDto;
import com.SpeakMate.Ai.friend.dto.AdminUserListDto;
import com.SpeakMate.Ai.friend.dto.admin.DailyOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.DashboardOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.MonthlyOverviewDto;
import com.SpeakMate.Ai.friend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/overview")
    public ResponseEntity<DashboardOverviewDto> getOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserListDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailsDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @GetMapping("/dashboard/monthly")
    public ResponseEntity<MonthlyOverviewDto> getMonthlyOverview(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(adminService.getMonthlyOverview(year, month));
    }

    @GetMapping("/dashboard/daily")
    public ResponseEntity<DailyOverviewDto> getDailyOverview(@RequestParam LocalDate date) {
        return ResponseEntity.ok(adminService.getDailyOverview(date));
    }
}
