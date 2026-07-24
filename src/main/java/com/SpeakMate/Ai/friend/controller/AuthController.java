package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.ForgotPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.dto.ResetPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyPasswordResetOtpRequestDto;
import com.SpeakMate.Ai.friend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                authService.sendRegistrationOtp(email)
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                authService.sendRegistrationOtp(email)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.verifyOtp(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/forgot-password/resend-otp")
    public ResponseEntity<String> resendPasswordResetOtp(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                authService.resendPasswordResetOtp(email)
        );
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<String> verifyPasswordResetOtp(
            @Valid @RequestBody VerifyPasswordResetOtpRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.verifyPasswordResetOtp(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }
}