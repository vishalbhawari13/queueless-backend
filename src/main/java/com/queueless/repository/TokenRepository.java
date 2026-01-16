package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    /* =======================
       ADMIN FLOW
       ======================= */

    List<Token> findByQueueAndStatusOrderByTokenNumberAsc(
            Queue queue,
            TokenStatus status
    );

    Optional<Token> findByQueueAndTokenNumber(
            Queue queue,
            int tokenNumber
    );

    /* =======================
       🔔 NOW SERVING TOKEN (FIX)
       ======================= */

    // ✅ LATEST CALLED TOKEN (highest token number)
    Optional<Token> findFirstByQueueAndStatusOrderByTokenNumberDesc(
            Queue queue,
            TokenStatus status
    );

    /* =======================
       TOKEN CREATION RULES
       ======================= */

    boolean existsByQueueAndPhone(
            Queue queue,
            String phone
    );

    long countByQueueAndCreatedAtAfter(
            Queue queue,
            LocalDateTime createdAfter
    );

    /* =======================
       ANALYTICS
       ======================= */

    long countByQueueAndStatus(
            Queue queue,
            TokenStatus status
    );

    @Query("""
        SELECT COALESCE(SUM(t.billAmount), 0)
        FROM Token t
        WHERE t.queue = :queue
          AND t.status = :status
    """)
    int sumBillAmountByQueueAndStatus(
            @Param("queue") Queue queue,
            @Param("status") TokenStatus status
    );
}
