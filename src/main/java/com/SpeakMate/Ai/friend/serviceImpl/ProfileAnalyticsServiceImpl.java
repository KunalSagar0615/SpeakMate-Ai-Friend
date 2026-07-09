package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.ProfileAnalyticsDto;
import com.SpeakMate.Ai.friend.entities.Session;
import com.SpeakMate.Ai.friend.repository.ConversationRepository;
import com.SpeakMate.Ai.friend.repository.SessionRepository;
import com.SpeakMate.Ai.friend.service.ProfileAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileAnalyticsServiceImpl implements ProfileAnalyticsService {

    private final SessionRepository sessionRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public ProfileAnalyticsDto getProfileAnalytics(Long userId) {

        List<Session> sessions = sessionRepository.findByUserIdOrderByStartTimeAsc(userId);
        long totalSessions = sessions.size();
        long totalConversations = conversationRepository.countByUserId(userId);

        double averageScore = conversationRepository.averageScoreByUserId(userId);

        long reportsGenerated = totalSessions;

        Set<LocalDate> practiceDates = sessions.stream().map(session -> session.getStartTime().toLocalDate())
                .collect(Collectors.toCollection(TreeSet::new));

        long totalPracticeDays = practiceDates.size();
        long currentStreak = calculateCurrentStreak(practiceDates);
        long longestStreak = calculateLongestStreak(practiceDates);

        Map<String, Long> modeUsage = sessions.stream().collect(Collectors.groupingBy( session -> session.getMode().name(),
                                Collectors.counting()));

        Map<String, Long> difficultyUsage = sessions.stream().collect(Collectors.groupingBy( session -> session.getDifficultyLevel().name(),
                                Collectors.counting() ));

        Map<String, Long> activityHeatmap = new LinkedHashMap<>();

        sessions.forEach(session -> {

            String date = session.getStartTime()
                            .toLocalDate()
                            .toString();

            activityHeatmap.put(
                    date,
                    activityHeatmap.getOrDefault(date, 0L) + 1
            );
        });

        return new ProfileAnalyticsDto(
                totalSessions,
                totalConversations,
                totalPracticeDays,
                currentStreak,
                longestStreak,
                averageScore,
                reportsGenerated,
                modeUsage,
                difficultyUsage,
                activityHeatmap
        );
    }

    private long calculateCurrentStreak(Set<LocalDate> practiceDates) {

        if (practiceDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        long streak = 0;

        while (practiceDates.contains(today)) {

            streak++;

            today = today.minusDays(1);
        }

        return streak;
    }

    private long calculateLongestStreak(Set<LocalDate> practiceDates) {

        if (practiceDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates = new ArrayList<>(practiceDates);

        long longest = 1;

        long current = 1;

        for (int i = 1; i < dates.size(); i++) {

            long diff =
                    ChronoUnit.DAYS.between(
                            dates.get(i - 1),
                            dates.get(i)
                    );

            if (diff == 1) {

                current++;

            } else {

                longest = Math.max(longest, current);

                current = 1;
            }
        }

        return Math.max(longest, current);
    }
}