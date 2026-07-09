package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;

public interface AuthService {

    String register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    String sendRegistrationOtp(String email);

    String verifyOtp(VerifyOtpRequestDto request);
}
