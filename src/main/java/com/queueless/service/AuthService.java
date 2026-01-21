package com.queueless.service;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AdminLoginRequest;
import com.queueless.entity.RefreshToken;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /* =====================================================
       ADMIN LOGIN
       ===================================================== */
    @Transactional
    public AuthResponse login(AdminLoginRequest request) {

        User admin = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Invalid credentials"));

        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            throw new BusinessException("Access denied");
        }

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String accessToken =
                jwtUtil.generateAccessToken(admin.getEmail());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshTokenForUser(admin);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    /* =====================================================
       REFRESH ACCESS TOKEN
       ===================================================== */
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {

        RefreshToken rotated =
                refreshTokenService
                        .verifyAndRotateRefreshToken(refreshToken);

        String newAccessToken =
                jwtUtil.generateAccessToken(
                        rotated.getUser().getEmail()
                );

        return new AuthResponse(
                newAccessToken,
                rotated.getToken()
        );
    }

    /* =====================================================
       RESPONSE
       ===================================================== */
    public record AuthResponse(
            String accessToken,
            String refreshToken
    ) {}
}
