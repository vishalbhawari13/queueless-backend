package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    /* ===============================
       READ-ONLY METHODS (NO LOCKS)
       =============================== */

    @Transactional(readOnly = true)
    Optional<Queue> findById(UUID id);

    @Transactional(readOnly = true)
    Optional<Queue> findByShopAndQueueDate(
            Shop shop,
            LocalDate queueDate
    );

    /* ===============================
       WRITE / ADMIN METHODS (LOCKED)
       =============================== */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Queue q WHERE q.id = :id")
    Optional<Queue> findByIdForUpdate(
            @Param("id") UUID id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT q FROM Queue q
        WHERE q.shop.id = :shopId
          AND q.status = :status
    """)
    Optional<Queue> findByShopIdAndStatusForUpdate(
            @Param("shopId") UUID shopId,
            @Param("status") QueueStatus status
    );
}
