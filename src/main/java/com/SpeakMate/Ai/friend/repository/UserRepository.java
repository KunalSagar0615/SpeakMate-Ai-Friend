package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.SpeakMate.Ai.friend.enumeration.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByRole(Role role);
}