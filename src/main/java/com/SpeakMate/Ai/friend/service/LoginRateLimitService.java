package com.SpeakMate.Ai.friend.service;

public interface LoginRateLimitService {

    boolean isBlocked(String username);

    void recordFailedAttempt(String username);

    void resetAttempts(String username);
}