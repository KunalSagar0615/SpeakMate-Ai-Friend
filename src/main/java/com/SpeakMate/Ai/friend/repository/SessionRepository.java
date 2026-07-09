package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserId(Long userId);

    List<Session> findByUserIdOrderByStartTimeAsc(Long userId);
}
