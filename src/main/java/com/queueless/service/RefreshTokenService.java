package com.queueless.service;

import com.queueless.entity.RefreshToken;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;

    // Token validity in days
    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    /** Create new refresh token for a user */
    @Transactional
    public RefreshToken createRefreshTokenForUser(User user) {

        // Delete old token if exists (one-to-one mapping)
        refreshTokenRepo.findByUser(user)
                .ifPresent(refreshTokenRepo::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryTime(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .build();

        return refreshTokenRepo.save(refreshToken);
    }

    /** Verify if refresh token is valid */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refreshToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(refreshToken);
            throw new BusinessException("Refresh token expired");
        }

        return refreshToken;
    }

}
