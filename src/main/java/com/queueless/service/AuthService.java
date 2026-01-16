package com.queueless.service;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AdminLoginRequest;
import com.queueless.entity.RefreshToken;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepo,
            BCryptPasswordEncoder encoder,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /* ================= ADMIN LOGIN ================= */

    @Transactional
    public AuthResponse login(AdminLoginRequest request) {

        User admin = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Invalid credentials"));

        if (!admin.getRole().equals("ROLE_ADMIN")) {
            throw new BusinessException("Access denied");
        }

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String accessToken =
                jwtUtil.generateAccessToken(admin.getEmail());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshTokenForUser(admin);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    /* ================= REFRESH ================= */

    @Transactional
    public String refreshAccessToken(String refreshToken) {

        RefreshToken token =
                refreshTokenService.verifyRefreshToken(refreshToken);

        return jwtUtil.generateAccessToken(
                token.getUser().getEmail()
        );
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken
    ) {}
}
