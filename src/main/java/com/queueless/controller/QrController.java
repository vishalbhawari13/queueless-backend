package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.exception.BusinessException;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/q/shop")
public class QrController {

    private final QueueService queueService;

    public QrController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/{shopId}")
    public Map<String, Object> openShopQueue(
            @PathVariable UUID shopId
    ) {

        Queue queue = queueService.getActiveQueueByShopId(shopId);

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is currently closed");
        }

        return Map.of(
                "shopId", shopId,
                "queueId", queue.getId(),
                "shopName", queue.getShop().getName(),
                "avgServiceTimeMinutes", queue.getAvgServiceTimeMinutes(),
                "queueOpen", true
        );
    }
}
