package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.CustomPracticeSession;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.CustomPracticeStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomPracticeSessionRepository
        extends JpaRepository<CustomPracticeSession, Long> {

    // =========================================================
    // OWNERSHIP LOOKUP
    // =========================================================

    Optional<CustomPracticeSession> findByIdAndUser(
            Long sessionId,
            User user
    );

    // =========================================================
    // OWNERSHIP LOOKUP WITH WRITE LOCK
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM CustomPracticeSession s
            WHERE s.id = :sessionId
              AND s.user = :user
            """)
    Optional<CustomPracticeSession> findByIdAndUserForUpdate(
            @Param("sessionId") Long sessionId,
            @Param("user") User user
    );

    // =========================================================
    // GENERIC SESSION WRITE LOCK
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM CustomPracticeSession s
            WHERE s.id = :sessionId
            """)
    Optional<CustomPracticeSession> findByIdForUpdate(
            @Param("sessionId") Long sessionId
    );

    // =========================================================
    // USER SESSIONS
    // =========================================================

    List<CustomPracticeSession> findAllByUserOrderByStartedAtDesc(
            User user
    );

    // =========================================================
    // USER SESSIONS BY STATUS
    // =========================================================

    List<CustomPracticeSession> findAllByUserAndStatusOrderByStartedAtDesc(
            User user,
            CustomPracticeStatus status
    );

    // =========================================================
    // USER SESSION HISTORY
    // =========================================================

    List<CustomPracticeSession> findAllByUserAndStatusInOrderByStartedAtDesc(
            User user,
            Collection<CustomPracticeStatus> statuses
    );

    // =========================================================
    // EXPIRATION CANDIDATES
    // =========================================================

    List<CustomPracticeSession> findAllByStatusAndExpiresAtBefore(
            CustomPracticeStatus status,
            LocalDateTime expiresAt
    );
}