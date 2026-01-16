package com.queueless.service;

import com.queueless.entity.RefreshToken;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    /* =====================================================
       CREATE REFRESH TOKEN (LOGIN)
       ===================================================== */

    @Transactional
    public RefreshToken createRefreshTokenForUser(User user) {

        // ✅ Enforce ONE refresh token per user
        refreshTokenRepo.findByUser(user)
                .ifPresent(refreshTokenRepo::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryTime(LocalDateTime.now()
                        .plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .build();

        return refreshTokenRepo.save(refreshToken);
    }

    /* =====================================================
       VERIFY + ROTATE REFRESH TOKEN (REFRESH API)
       ===================================================== */

    @Transactional
    public RefreshToken verifyAndRotateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() ->
                        new BusinessException("Invalid refresh token"));

        if (refreshToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(refreshToken);
            throw new BusinessException("Refresh token expired");
        }

        // 🔁 Rotate token (security best practice)
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryTime(
                LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
        );

        return refreshTokenRepo.save(refreshToken);
    }
}
