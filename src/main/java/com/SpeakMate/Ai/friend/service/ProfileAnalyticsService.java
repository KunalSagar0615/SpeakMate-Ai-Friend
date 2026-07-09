package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.ProfileAnalyticsDto;

public interface ProfileAnalyticsService {

    ProfileAnalyticsDto getProfileAnalytics(Long userId);
}