package com.queueless.service;

import com.queueless.config.JwtUtil;
import com.queueless.dto.LoginRequest;
import com.queueless.dto.RegisterRequest;
import com.queueless.dto.VerifyOtpRequest;
import com.queueless.entity.EmailOtp;
import com.queueless.entity.RefreshToken;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.EmailOtpRepository;
import com.queueless.repository.UserRepository;
import jakarta.transaction.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserAuthService {

    private final UserRepository userRepo;
    private final EmailOtpRepository otpRepo;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserAuthService(
            UserRepository userRepo,
            EmailOtpRepository otpRepo,
            BCryptPasswordEncoder encoder,
            EmailService emailService,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepo = userRepo;
        this.otpRepo = otpRepo;
        this.encoder = encoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /* ================= REGISTER ================= */

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }

        User user = userRepo.save(
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(encoder.encode(request.getPassword()))
                        .verified(false)
                        .role("ROLE_USER")
                        .build()
        );

        sendOtp(user.getEmail());
    }

    /* ================= SEND OTP ================= */

    public void sendOtp(String email) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        otpRepo.deleteByEmail(email);

        otpRepo.save(
                EmailOtp.builder()
                        .email(email)
                        .otp(otp)
                        .expiryTime(LocalDateTime.now().plusMinutes(10))
                        .build()
        );

        emailService.sendOtp(email, otp);
    }

    /* ================= VERIFY OTP ================= */

    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {

        EmailOtp otp = otpRepo.findByEmailAndOtp(
                request.getEmail(),
                request.getOtp()
        ).orElseThrow(() -> new BusinessException("Invalid OTP"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP expired");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();

        user.setVerified(true);
        userRepo.save(user);

        otpRepo.deleteByEmail(request.getEmail());
    }

    /* ================= LOGIN ================= */

    public AuthResponse login(LoginRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!user.isVerified()) {
            throw new BusinessException("Verify email first");
        }

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        RefreshToken refreshToken =
                refreshTokenService.createRefreshTokenForUser(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public record AuthResponse(String accessToken, String refreshToken) {}
}
