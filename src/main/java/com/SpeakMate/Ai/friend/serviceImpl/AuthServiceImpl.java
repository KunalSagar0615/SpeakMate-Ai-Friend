package com.SpeakMate.Ai.friend.serviceImpl;

import com.SpeakMate.Ai.friend.dto.AuthResponseDto;
import com.SpeakMate.Ai.friend.dto.LoginRequestDto;
import com.SpeakMate.Ai.friend.dto.RegisterRequestDto;
import com.SpeakMate.Ai.friend.entities.EmailVerificationOtp;
import com.SpeakMate.Ai.friend.entities.User;
import com.SpeakMate.Ai.friend.enumeration.Role;
import com.SpeakMate.Ai.friend.exception.*;
import com.SpeakMate.Ai.friend.security.JwtUtil;
import com.SpeakMate.Ai.friend.service.AuthService;
import org.springframework.stereotype.Service;
import com.SpeakMate.Ai.friend.dto.VerifyOtpRequestDto;
import com.SpeakMate.Ai.friend.exception.InvalidOtpException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import com.SpeakMate.Ai.friend.repository.EmailVerificationOtpRepository;
import com.SpeakMate.Ai.friend.repository.UserRepository;
import com.SpeakMate.Ai.friend.service.MailService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    private final EmailVerificationOtpRepository otpRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    public AuthServiceImpl(
            UserRepository userRepository,
            EmailVerificationOtpRepository otpRepository,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String register(
            RegisterRequestDto request
    ) {

        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new EmailAlreadyExistsException(
                    "Email already registered"
            );
        }

        if (userRepository.existsByUsername(
                request.getUsername()
        )) {

            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setMobileNumber(
                request.getMobileNumber()
        );

        user.setHighestEducation(
                request.getHighestEducation()
        );

        user.setCurrentOccupation(
                request.getCurrentOccupation()
        );

        user.setCountry(
                request.getCountry()
        );

        user.setRole(Role.USER);

        user.setEmailVerified(false);

        userRepository.save(user);

        sendRegistrationOtp(
                request.getEmail()
        );

        return "Registration successful. OTP sent to your email.";
    }

    @Override
    public AuthResponseDto login(
            LoginRequestDto request
    ) {

        User user = userRepository
                .findByUsername(
                        request.getUsername()
                )
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found"
                        ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        if (!user.getEmailVerified()) {

            throw new EmailNotVerifiedException(
                    "Please verify your email first"
            );
        }
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
        return String.valueOf(100000 + (int)(Math.random() * 900000));
    }

    private EmailVerificationOtp createOtpEntity(String email, String otp) {
        EmailVerificationOtp emailOtp = new EmailVerificationOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setAttemptCount(0);
        emailOtp.setVerified(false);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        return emailOtp;
    }

    public String sendRegistrationOtp(String email) {

        String otp = generateOtp();

        EmailVerificationOtp emailOtp = createOtpEntity(email, otp);

        otpRepository.save(emailOtp);

        mailService.sendOtpEmail(
                email,
                otp
        );

        return "OTP sent successfully";
    }

    @Override
    public String verifyOtp(
            VerifyOtpRequestDto request
    ) {

        EmailVerificationOtp emailOtp =
                otpRepository
                        .findTopByEmailOrderByCreatedAtDesc(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "OTP not found"
                                ));

        if (emailOtp.getVerified()) {
            return "Email already verified";
        }

        if (emailOtp.getAttemptCount() >= 3) {
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
                .equals(request.getOtp())) {

            emailOtp.setAttemptCount(
                    emailOtp.getAttemptCount() + 1
            );

            otpRepository.save(emailOtp);

            throw new InvalidOtpException(
                    "Invalid OTP"
            );
        }

        emailOtp.setVerified(true);

        otpRepository.save(emailOtp);

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "User not found"
                ));

        user.setEmailVerified(true);

        userRepository.save(user);

        return "OTP verified successfully";
    }


}