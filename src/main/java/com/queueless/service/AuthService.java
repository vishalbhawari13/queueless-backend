package com.queueless.service;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AdminLoginRequest;
import com.queueless.entity.AdminUser;
import com.queueless.entity.RefreshToken;
import com.queueless.exception.BusinessException;
import com.queueless.repository.AdminUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminUserRepository adminRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AdminUserRepository adminRepo,
            BCryptPasswordEncoder encoder,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.adminRepo = adminRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse login(AdminLoginRequest request) {

        AdminUser admin = adminRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("Invalid credentials");
        }

        String accessToken =
                jwtUtil.generateAccessToken(admin.getUsername());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(admin);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {

        RefreshToken token =
                refreshTokenService.verifyRefreshToken(refreshToken);

        return jwtUtil.generateAccessToken(
                token.getAdminUser().getUsername()
        );
    }

    // simple DTO
    public record AuthResponse(
            String accessToken,
            String refreshToken
    ) {}
}
