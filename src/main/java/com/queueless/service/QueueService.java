package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.exception.BusinessException;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final ShopRepository shopRepository;

    public QueueService(
            QueueRepository queueRepository,
            ShopRepository shopRepository
    ) {
        this.queueRepository = queueRepository;
        this.shopRepository = shopRepository;
    }

    /* ===============================
       PUBLIC – READ ONLY (STRICT)
       =============================== */
    @Transactional(readOnly = true)
    public Queue getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new BusinessException("Invalid queue"));
    }

    /* ===============================
       ADMIN – READ ONLY (STRICT)
       =============================== */
    @Transactional(readOnly = true)
    public Queue getActiveQueueByShopId(UUID shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Shop not found"));

        return queueRepository
                .findByShopAndQueueDate(shop, LocalDate.now())
                .filter(q -> q.getStatus() == QueueStatus.OPEN)
                .orElseThrow(() ->
                        new BusinessException("No active queue for today"));
    }

    /* ===============================
       CONTEXT / DASHBOARD – SAFE READ
       =============================== */
    @Transactional(readOnly = true)
    public Queue getActiveQueueIfExists(UUID shopId) {

        return shopRepository.findById(shopId)
                .flatMap(shop ->
                        queueRepository
                                .findByShopAndQueueDate(shop, LocalDate.now())
                                .filter(q -> q.getStatus() == QueueStatus.OPEN)
                )
                .orElse(null);
    }

    /* ===============================
       WRITE OPERATION
       =============================== */
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
                .orElseGet(() ->
                        queueRepository.save(
                                Queue.builder()
                                        .shop(shop)
                                        .queueDate(LocalDate.now())
                                        .status(QueueStatus.OPEN)
                                        .currentToken(0)
                                        .avgServiceTimeMinutes(5)
                                        .build()
                        )
                );
    }
}
