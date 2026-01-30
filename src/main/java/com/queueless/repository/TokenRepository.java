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

    /* ===============================
       BASIC TOKEN FLOW
       =============================== */

    List<Token> findByQueueAndStatusOrderByTokenNumberAsc(
            Queue queue,
            TokenStatus status
    );

    Optional<Token> findByQueueAndTokenNumber(
            Queue queue,
            int tokenNumber
    );

    Optional<Token> findFirstByQueueAndStatusOrderByTokenNumberAsc(
            Queue queue,
            TokenStatus status
    );
    long countByQueueAndStatus(Queue queue, TokenStatus status);

    Optional<Token> findFirstByQueueAndStatusOrderByTokenNumberDesc(
            Queue queue,
            TokenStatus status
    );

    boolean existsByQueueAndPhone(
            Queue queue,
            String phone
    );

    long countByQueueAndCreatedAtAfter(
            Queue queue,
            LocalDateTime createdAfter
    );

    /* ===============================
       📊 DAILY ANALYTICS (SAFE)
       =============================== */

    @Query("""
        SELECT COUNT(t)
        FROM Token t
        WHERE t.queue.shop.id = :shopId
          AND t.status = com.queueless.entity.enums.TokenStatus.COMPLETED
          AND t.createdAt >= CURRENT_DATE
    """)
    int countCompletedToday(@Param("shopId") UUID shopId);

    @Query("""
        SELECT COALESCE(SUM(t.billAmount), 0)
        FROM Token t
        WHERE t.queue.shop.id = :shopId
          AND t.status = com.queueless.entity.enums.TokenStatus.COMPLETED
          AND t.createdAt >= CURRENT_DATE
    """)
    int sumRevenueToday(@Param("shopId") UUID shopId);

    /* ===============================
       📈 MONTHLY ANALYTICS (SAFE)
       =============================== */

    @Query("""
        SELECT COUNT(t)
        FROM Token t
        WHERE t.queue.shop.id = :shopId
          AND t.status = com.queueless.entity.enums.TokenStatus.COMPLETED
          AND FUNCTION('MONTH', t.createdAt) = :month
          AND FUNCTION('YEAR', t.createdAt) = FUNCTION('YEAR', CURRENT_DATE)
        GROUP BY FUNCTION('DAY', t.createdAt)
        ORDER BY FUNCTION('DAY', t.createdAt)
    """)
    List<Integer> dailyCountsForMonth(
            @Param("shopId") UUID shopId,
            @Param("month") int month
    );

    @Query("""
    SELECT COALESCE(MAX(t.tokenNumber), 0)
    FROM Token t
    WHERE t.queue = :queue
""")
    int findMaxTokenNumber(@Param("queue") Queue queue);

}
