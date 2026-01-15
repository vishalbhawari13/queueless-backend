package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.enums.QueueStatus;
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

    @Transactional
    public Queue createOrGetTodayQueue(UUID shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return queueRepository
                .findByShopAndQueueDate(shop, LocalDate.now())
                .orElseGet(() -> queueRepository.save(
                        Queue.builder()
                                .shop(shop)
                                .status(QueueStatus.OPEN)
                                .currentToken(0)
                                .queueDate(LocalDate.now())
                                .build()
                ));
    }

    @Transactional
    public Queue getActiveQueue(UUID shopId) {

        return queueRepository
                .findByShopIdAndStatus(shopId, QueueStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("No active queue"));
    }

}
