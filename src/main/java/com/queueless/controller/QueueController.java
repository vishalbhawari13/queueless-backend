package com.queueless.controller;

import com.queueless.dto.QueueRequest;
import com.queueless.entity.Queue;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    /* ===============================
       CREATE / REOPEN TODAY QUEUE
       =============================== */
    @PostMapping("/create")
    public Queue createQueue(@RequestBody QueueRequest request) {

        return queueService.createOrGetTodayQueue(
                request.getShopId()
        );
    }

    /* ===============================
       QUEUE STATUS (READ-ONLY)
       =============================== */
    @GetMapping("/status/{shopId}")
    public Queue getQueueStatus(@PathVariable UUID shopId) {

        return queueService.getActiveQueueByShopId(shopId);
    }
}
