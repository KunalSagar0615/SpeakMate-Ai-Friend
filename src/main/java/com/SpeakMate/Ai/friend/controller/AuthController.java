package com.SpeakMate.Ai.friend.controller;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;
import com.SpeakMate.Ai.friend.service.AuthService;
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
            @RequestBody RegisterRequestDto request
    ) {

        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody VerifyOtpRequestDto request
    ) {

        return ResponseEntity.ok(
                authService.verifyOtp(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}