package com.queueless.repository;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    Optional<Queue> findByShopAndQueueDate(Shop shop, LocalDate queueDate);

    Optional<Queue> findByShopAndStatus(Shop shop, QueueStatus status);
}
