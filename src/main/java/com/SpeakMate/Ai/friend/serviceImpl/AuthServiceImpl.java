package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.ForgotPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.dto.ResetPasswordRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;
import com.SpeakMate.Ai.friend.dto.VerifyPasswordResetOtpRequestDto;
import com.SpeakMate.Ai.friend.entities.EmailVerificationOtp;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.OtpPurpose;
import com.SpeakMate.Ai.friend.enumeration.Role;
import com.SpeakMate.Ai.friend.exception.EmailAlreadyExistsException;
import com.SpeakMate.Ai.friend.exception.EmailNotVerifiedException;
import com.SpeakMate.Ai.friend.exception.InvalidCredentialsException;
import com.SpeakMate.Ai.friend.exception.InvalidOtpException;
import com.SpeakMate.Ai.friend.exception.MaximumOtpAttemptsExceededException;
import com.SpeakMate.Ai.friend.exception.OtpExpiredException;
import com.SpeakMate.Ai.friend.exception.ResourceNotFoundException;
import com.SpeakMate.Ai.friend.exception.UserNotFoundException;
import com.SpeakMate.Ai.friend.exception.UsernameAlreadyExistsException;
import com.SpeakMate.Ai.friend.repository.EmailVerificationOtpRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.security.JwtUtil;
import com.SpeakMate.Ai.friend.service.AuthService;
import com.SpeakMate.Ai.friend.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import com.SpeakMate.Ai.friend.service.LoginRateLimitService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailVerificationOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final LoginRateLimitService loginRateLimitService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
            EmailVerificationOtpRepository otpRepository,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            LoginRateLimitService loginRateLimitService
    ) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginRateLimitService = loginRateLimitService;
    }

    @Override
    public String register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNumber(request.getMobileNumber());
        user.setHighestEducation(request.getHighestEducation());
        user.setCurrentOccupation(request.getCurrentOccupation());
        user.setCountry(request.getCountry());
        user.setRole(Role.USER);
        user.setEmailVerified(false);

        userRepository.save(user);

        sendRegistrationOtp(request.getEmail());

        return "Registration successful. OTP sent to your email.";
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        String username = request.getUsername();

        // 1. Check whether the account is temporarily blocked
        if (loginRateLimitService.isBlocked(username)) {
            throw new InvalidCredentialsException(
                    "Too many failed login attempts. Please try again after 15 minutes."
            );
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        // 2. Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            // Record failed attempt
            loginRateLimitService.recordFailedAttempt(username);

            // Send security alert after the 5th failed attempt
            if (loginRateLimitService.isBlocked(username)) {
                mailService.sendLoginFailedAlertEmail(user.getEmail());
            }

            throw new InvalidCredentialsException("Invalid username or password");
        }

        // 3. Successful login → reset failed attempts
        loginRateLimitService.resetAttempts(username);

        // 4. Check email verification
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException(
                    "Please verify your email first"
            );
        }

        // 5. Generate JWT
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponseDto(
                token,
                user.getUsername(),
                user.getName(),
                user.getRole().name()
        );
    }

    private String generateOtp() {
        return String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );
    }

    private EmailVerificationOtp createOtpEntity(
            String email,
            String otp,
            OtpPurpose purpose
    ) {

        EmailVerificationOtp emailOtp =
                new EmailVerificationOtp();

        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setAttemptCount(0);
        emailOtp.setVerified(false);
        emailOtp.setPurpose(purpose);

        emailOtp.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(OTP_EXPIRY_MINUTES)
        );

        return emailOtp;
    }

    private String sendOtp(
            String email,
            OtpPurpose purpose
    ) {

        String otp = generateOtp();

        EmailVerificationOtp emailOtp =
                createOtpEntity(
                        email,
                        otp,
                        purpose
                );

        otpRepository.save(emailOtp);

        mailService.sendOtpEmail(
                email,
                otp
        );

        return "OTP sent successfully";
    }

    @Override
    public String sendRegistrationOtp(String email) {

        return sendOtp(
                email,
                OtpPurpose.EMAIL_VERIFICATION
        );
    }

    @Override
    public String verifyOtp(
            VerifyOtpRequestDto request
    ) {

        EmailVerificationOtp emailOtp =
                otpRepository
                        .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                                request.getEmail(),
                                OtpPurpose.EMAIL_VERIFICATION
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "OTP not found"
                                )
                        );

        validateOtp(
                emailOtp,
                request.getOtp()
        );

        if (Boolean.TRUE.equals(emailOtp.getVerified())) {
            return "Email already verified";
        }

        emailOtp.setVerified(true);
        otpRepository.save(emailOtp);

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        user.setEmailVerified(true);
        userRepository.save(user);

        return "OTP verified successfully";
    }

    @Override
    public String forgotPassword(
            ForgotPasswordRequestDto request
    ) {

        String email = request.getEmail();

        userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "No account found with this email"
                        )
                );

        sendOtp(
                email,
                OtpPurpose.PASSWORD_RESET
        );

        return "Password reset OTP sent successfully";
    }

    @Override
    public String resendPasswordResetOtp(
            String email
    ) {

        userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "No account found with this email"
                        )
                );

        sendOtp(
                email,
                OtpPurpose.PASSWORD_RESET
        );

        return "Password reset OTP resent successfully";
    }

    @Override
    public String verifyPasswordResetOtp(
            VerifyPasswordResetOtpRequestDto request
    ) {

        EmailVerificationOtp emailOtp =
                otpRepository
                        .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                                request.getEmail(),
                                OtpPurpose.PASSWORD_RESET
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Password reset OTP not found"
                                )
                        );

        if (Boolean.TRUE.equals(emailOtp.getVerified())) {
            return "Password reset OTP already verified";
        }

        validateOtp(
                emailOtp,
                request.getOtp()
        );

        emailOtp.setVerified(true);
        otpRepository.save(emailOtp);

        return "Password reset OTP verified successfully";
    }

    @Override
    @Transactional
    public String resetPassword(
            ResetPasswordRequestDto request
    ) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new InvalidCredentialsException(
                    "New password and confirm password do not match"
            );
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        )
                );

        EmailVerificationOtp emailOtp =
                otpRepository
                        .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                                request.getEmail(),
                                OtpPurpose.PASSWORD_RESET
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Password reset request not found"
                                )
                        );

        if (!Boolean.TRUE.equals(emailOtp.getVerified())) {
            throw new InvalidOtpException(
                    "Please verify your password reset OTP first"
            );
        }

        if (emailOtp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new OtpExpiredException(
                    "Password reset session has expired. Please request a new OTP."
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        otpRepository.deleteByEmailAndPurpose(
                request.getEmail(),
                OtpPurpose.PASSWORD_RESET
        );

        return "Password reset successfully";
    }

    private void validateOtp(
            EmailVerificationOtp emailOtp,
            String submittedOtp
    ) {

        if (emailOtp.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            throw new MaximumOtpAttemptsExceededException(
                    "Maximum OTP attempts exceeded"
            );
        }

        if (emailOtp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new OtpExpiredException(
                    "OTP has expired"
            );
        }

        if (!emailOtp.getOtp()
                .equals(submittedOtp)) {

            emailOtp.setAttemptCount(
                    emailOtp.getAttemptCount() + 1
            );

            otpRepository.save(emailOtp);

            throw new InvalidOtpException(
                    "Invalid OTP"
            );
        }
    }
}