package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.TongueTwisterSession;
import com.SpeakMate.Ai.friend.enumeration.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TongueTwisterSessionRepository
        extends JpaRepository<TongueTwisterSession, Long> {

    List<TongueTwisterSession> findByUserIdOrderByStartTimeDesc(Long userId);

    Optional<TongueTwisterSession> findFirstByUserIdAndStatusOrderByStartTimeDesc(Long userId,SessionStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, SessionStatus status);
}