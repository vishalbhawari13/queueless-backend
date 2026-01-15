package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    List<Token> findByQueueAndStatusOrderByTokenNumberAsc(
            Queue queue,
            TokenStatus status
    );

    Optional<Token> findByQueueAndTokenNumber(
            Queue queue,
            int tokenNumber
    );
}
