package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByQueueAndPhone(Queue queue, String phone);

    long countByQueueAndStatus(
            Queue queue,
            com.queueless.entity.enums.TokenStatus status
    );

    @Query("SELECT COALESCE(SUM(t.billAmount),0) FROM Token t WHERE t.queue = :queue AND t.status = :status")
    Integer sumBillAmountByQueueAndStatus(
            @Param("queue") Queue queue,
            @Param("status") TokenStatus status
    );







}
