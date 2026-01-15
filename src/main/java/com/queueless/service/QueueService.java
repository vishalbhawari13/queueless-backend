package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.exception.BusinessException;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final ShopRepository shopRepository;

    public QueueService(QueueRepository queueRepository,
                        ShopRepository shopRepository) {
        this.queueRepository = queueRepository;
        this.shopRepository = shopRepository;
    }

    /**
     * Create OR reopen today's queue
     */
    @Transactional
    public Queue createOrGetTodayQueue(UUID shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Shop not found"));

        return queueRepository
                .findByShopAndQueueDate(shop, LocalDate.now())
                .map(queue -> {
                    if (queue.getStatus() == QueueStatus.CLOSED) {
                        queue.setStatus(QueueStatus.OPEN);
                        return queueRepository.save(queue);
                    }
                    return queue;
                })
                .orElseGet(() -> queueRepository.save(
                        Queue.builder()
                                .shop(shop)
                                .status(QueueStatus.OPEN)
                                .currentToken(0)
                                .avgServiceTimeMinutes(5)
                                .queueDate(LocalDate.now())
                                .build()
                ));
    }

    /**
     * ✅ USED BY ADMIN APIs
     * Active queue with DB lock
     */
    @Transactional
    public Queue getActiveQueueByShopId(UUID shopId) {
        return queueRepository
                .findByShopIdAndStatus(shopId, QueueStatus.OPEN)
                .orElseThrow(() -> new BusinessException("No active queue"));
    }

    /**
     * Used by public APIs (QR, token creation)
     */
    @Transactional
    public Queue getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new BusinessException("Invalid queue"));
    }
}
