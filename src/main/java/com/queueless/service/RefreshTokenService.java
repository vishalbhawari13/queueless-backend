package com.queueless.service;

import com.queueless.entity.AdminUser;
import com.queueless.entity.RefreshToken;
import com.queueless.exception.BusinessException;
import com.queueless.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_DAYS = 30;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Create or replace refresh token
     * GUARANTEED single token per admin
     */
    @Transactional
    public RefreshToken createRefreshToken(AdminUser admin) {

        // 🔥 HARD DELETE existing token (no flush issues)
        refreshTokenRepository.deleteByAdminUser(admin);

        RefreshToken refreshToken = RefreshToken.builder()
                .adminUser(admin)
                .token(UUID.randomUUID().toString())
                .expiryDate(
                        LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS)
                )
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verify refresh token
     */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BusinessException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token expired");
        }

        return refreshToken;
    }
}
