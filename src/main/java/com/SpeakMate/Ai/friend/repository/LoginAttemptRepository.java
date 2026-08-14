package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsername(String username);

    void deleteByUsername(String username);
}