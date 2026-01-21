package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/shop")
public class PublicShopQueueController {

    private final QueueService queueService;

    public PublicShopQueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/{shopId}")
    public Map<String, Object> getLiveShopQueue(
            @PathVariable UUID shopId
    ) {

        Queue queue = queueService.getActiveQueueByShopId(shopId);

        return Map.of(
                "shopId", shopId,
                "queueId", queue.getId(),
                "shopName", queue.getShop().getName(),
                "avgServiceTimeMinutes", queue.getAvgServiceTimeMinutes(),
                "currentToken", queue.getCurrentToken(),
                "queueOpen", true
        );
    }
}
