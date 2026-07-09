package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.ProfileAnalyticsDto;
import com.SpeakMate.Ai.friend.dto.UserProfileDto;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.mapper.UserMapper;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.ProfileAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileAnalyticsController {

    private final ProfileAnalyticsService profileAnalyticsService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/me")
    public UserProfileDto getCurrentUserProfile(Authentication authentication) {
        return UserMapper.toProfileDto(getAuthenticatedUser(authentication));
    }

    @GetMapping("/analytics")
    public ProfileAnalyticsDto getAnalytics(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return profileAnalyticsService
                .getProfileAnalytics(
                        user.getId()
                );
    }
}