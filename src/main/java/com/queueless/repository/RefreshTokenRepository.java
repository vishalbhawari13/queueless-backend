package com.queueless.repository;

import com.queueless.entity.AdminUser;
import com.queueless.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find refresh token for an admin
     * (used to ensure single active session)
     */
    Optional<RefreshToken> findByAdminUser(AdminUser adminUser);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.adminUser = :adminUser")
    void deleteByAdminUser(AdminUser adminUser);
}
