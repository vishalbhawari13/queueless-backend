package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    Optional<Queue> findByShopAndQueueDate(Shop shop, LocalDate queueDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Queue> findByShopIdAndStatus(UUID shopId, QueueStatus status);

}
