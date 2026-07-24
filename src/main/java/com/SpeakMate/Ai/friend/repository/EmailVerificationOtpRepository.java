package com.SpeakMate.Ai.friend.repository;

import com.SpeakMate.Ai.friend.entities.EmailVerificationOtp;
import com.SpeakMate.Ai.friend.enumeration.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationOtpRepository
        extends JpaRepository<EmailVerificationOtp, Long> {

    Optional<EmailVerificationOtp> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email,OtpPurpose purpose);

    void deleteByEmailAndPurpose(String email,OtpPurpose purpose);
}