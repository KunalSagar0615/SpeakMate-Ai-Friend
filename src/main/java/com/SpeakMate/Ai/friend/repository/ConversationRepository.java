package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    List<Conversation> findBySessionId(Long sessionId);
    long countBySessionId(Long sessionId);

    @Query("SELECT COALESCE(SUM(c.score),0) FROM Conversation c WHERE c.session.id = :sessionId")
    Integer sumScoreBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COALESCE(AVG(c.score),0) FROM Conversation c WHERE c.session.id = :sessionId")
    Double averageScoreBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.session.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(c.score),0) FROM Conversation c WHERE c.session.user.id = :userId")
    Double averageScoreByUserId(@Param("userId") Long userId);
}
