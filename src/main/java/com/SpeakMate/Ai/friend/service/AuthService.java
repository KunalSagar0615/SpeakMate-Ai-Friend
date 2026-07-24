package com.SpeakMate.Ai.friend.service;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.ForgotPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.dto.ResetPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyPasswordResetOtpRequestDto;

public interface AuthService {

    String register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    String sendRegistrationOtp(String email);

    String verifyOtp(VerifyOtpRequestDto request);

    String forgotPassword(ForgotPasswordRequestDto request);

    String resendPasswordResetOtp(String email);

    String verifyPasswordResetOtp(
            VerifyPasswordResetOtpRequestDto request
    );

    String resetPassword(
            ResetPasswordRequestDto request
    );
}