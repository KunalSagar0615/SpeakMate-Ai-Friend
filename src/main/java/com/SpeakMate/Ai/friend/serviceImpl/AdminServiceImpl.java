package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.AdminUserDetailsDto;
import com.SpeakMate.Ai.friend.dto.AdminUserListDto;
import com.SpeakMate.Ai.friend.dto.UserSessionSummaryDto;
import com.SpeakMate.Ai.friend.dto.admin.DailyOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.DashboardOverviewDto;
import com.SpeakMate.Ai.friend.dto.admin.MonthlyOverviewDto;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.repository.ConversationRepository;
import com.SpeakMate.Ai.friend.repository.SessionRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    private final SessionRepository sessionRepository;

    private final ConversationRepository conversationRepository;

    @Override
    public DashboardOverviewDto getOverview() {

        long totalUsers = userRepository.count();

        long totalSessions = sessionRepository.count();

        long totalConversations = conversationRepository.count();

        return new DashboardOverviewDto(
                totalUsers,
                totalSessions,
                totalConversations
        );
    }

    @Override
    public MonthlyOverviewDto getMonthlyOverview(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        LocalDateTime start = LocalDateTime.of(
                year,
                month,
                1,
                0,
                0
        );

        LocalDateTime end = start.plusMonths(1);

        long activeUsers = sessionRepository.countActiveUsersBetween(start, end);
        long newUsers = userRepository.countByCreatedAtBetween(start, end);
        long totalSessions = sessionRepository.countByStartTimeBetween(start, end);
        long totalConversations = conversationRepository.countBySessionStartTimeBetween(start, end);

        return new MonthlyOverviewDto(
                year,
                month,
                activeUsers,
                newUsers,
                totalSessions,
                totalConversations
        );
    }

    @Override
    public DailyOverviewDto getDailyOverview(LocalDate date) {

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long activeUsers = sessionRepository.countActiveUsersBetween(start, end);
        long newUsers = userRepository.countByCreatedAtBetween(start, end);
        long totalSessions = sessionRepository.countByStartTimeBetween(start, end);
        long totalConversations = conversationRepository.countBySessionStartTimeBetween(start, end);

        return new DailyOverviewDto(
                date,
                activeUsers,
                newUsers,
                totalSessions,
                totalConversations
        );
    }

    @Override
    public List<AdminUserListDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new AdminUserListDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getMobileNumber()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailsDto getUser(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        List<UserSessionSummaryDto> sessions = user.getSessions()
                .stream()
                .map(session -> new UserSessionSummaryDto(
                        session.getId(),
                        session.getTopic(),
                        session.getMode(),
                        session.getDifficultyLevel(),
                        session.getStatus(),
                        session.getStartTime()
                ))
                .toList();

        return new AdminUserDetailsDto(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getCountry(),
                user.getHighestEducation(),
                user.getCurrentOccupation(),
                user.getRole(),
                user.getEmailVerified(),
                user.getCreatedAt(),
                sessions
        );
    }
}
