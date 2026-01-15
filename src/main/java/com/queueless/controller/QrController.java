package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.exception.BusinessException;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/q")
public class QrController {

    private final QueueService queueService;

    public QrController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/{queueId}")
    public Map<String, Object> validateQueue(@PathVariable UUID queueId) {

        Queue queue = queueService.getQueueById(queueId);

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        return Map.of(
                "queueId", queue.getId(),
                "shopName", queue.getShop().getName(),
                "queueDate", queue.getQueueDate()
        );
    }
}
