package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.TongueTwisterPassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TongueTwisterPassageRepository
        extends JpaRepository<TongueTwisterPassage, Long> {

    List<TongueTwisterPassage> findTop15ByUserIdOrderByCreatedAtDesc(Long userId);

    List<TongueTwisterPassage> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}