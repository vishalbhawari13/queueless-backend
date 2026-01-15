package com.queueless.repository;

import com.queueless.entity.TokenAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TokenAttemptRepository
        extends JpaRepository<TokenAttempt, UUID> {

    long countByPhoneAndCreatedAtAfter(
            String phone,
            LocalDateTime time
    );
}
