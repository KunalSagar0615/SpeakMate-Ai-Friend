package com.SpeakMate.Ai.friend.service;

public interface MailService {

    void sendOtpEmail(
            String email,
            String otp
    );
}