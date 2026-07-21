package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.SpeakMate.Ai.friend.enumeration.DifficultyLevel;
import com.SpeakMate.Ai.friend.enumeration.SessionMode;
import org.springframework.data.domain.Pageable;

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

    @Query("""
SELECT c
FROM Conversation c
WHERE c.session.user.id = :userId
  AND c.session.topic = :topic
  AND c.session.mode = :mode
  AND c.session.difficultyLevel = :difficultyLevel
ORDER BY c.id DESC
""")
    List<Conversation> findRecentQuestionsByUserAndTopicAndModeAndDifficulty(
            @Param("userId") Long userId,
            @Param("topic") String topic,
            @Param("mode") SessionMode mode,
            @Param("difficultyLevel") DifficultyLevel difficultyLevel,
            Pageable pageable
    );

}
