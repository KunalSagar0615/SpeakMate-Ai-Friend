package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserId(Long userId);

    List<Session> findByUserIdOrderByStartTimeAsc(Long userId);

    long count();

    long countByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
       SELECT COUNT(DISTINCT s.user.id)
       FROM Session s
       WHERE s.startTime >= :start
       AND s.startTime < :end
       """)
    long countActiveUsersBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
