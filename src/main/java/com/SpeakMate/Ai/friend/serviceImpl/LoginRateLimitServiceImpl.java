package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.entities.LoginAttempt;
import com.SpeakMate.Ai.friend.repository.LoginAttemptRepository;
import com.SpeakMate.Ai.friend.service.LoginRateLimitService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginRateLimitServiceImpl implements LoginRateLimitService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final LoginAttemptRepository loginAttemptRepository;

    public LoginRateLimitServiceImpl(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Override
    public boolean isBlocked(String username) {

        return loginAttemptRepository
                .findByUsername(username)
                .map(loginAttempt -> {

                    if (loginAttempt.getLockedUntil() == null) {
                        return false;
                    }

                    if (loginAttempt.getLockedUntil().isAfter(LocalDateTime.now())) {
                        return true;
                    }

                    // Lock has expired
                    loginAttempt.setFailedAttempts(0);
                    loginAttempt.setLockedUntil(null);

                    loginAttemptRepository.save(loginAttempt);

                    return false;
                })
                .orElse(false);
    }

    @Override
    public void recordFailedAttempt(String username) {

        LoginAttempt loginAttempt = loginAttemptRepository
                        .findByUsername(username)
                        .orElseGet(() -> {
                            LoginAttempt newAttempt = new LoginAttempt();

                            newAttempt.setUsername(username);
                            newAttempt.setFailedAttempts(0);

                            return newAttempt;
                        });

        int failedAttempts = loginAttempt.getFailedAttempts() + 1;

        loginAttempt.setFailedAttempts(failedAttempts);
        loginAttempt.setLastAttemptAt(LocalDateTime.now());

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            loginAttempt.setLockedUntil( LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        loginAttemptRepository.save(loginAttempt);
    }

    @Override
    public void resetAttempts(String username) {

        loginAttemptRepository
                .findByUsername(username)
                .ifPresent(loginAttempt -> {

                    loginAttempt.setFailedAttempts(0);
                    loginAttempt.setLastAttemptAt(null);
                    loginAttempt.setLockedUntil(null);

                    loginAttemptRepository.save(loginAttempt);
                });
    }
}