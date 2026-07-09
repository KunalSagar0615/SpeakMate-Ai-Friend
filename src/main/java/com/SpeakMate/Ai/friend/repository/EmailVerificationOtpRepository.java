package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {
    Optional<EmailVerificationOtp> findTopByEmailOrderByCreatedAtDesc(String email);
}
