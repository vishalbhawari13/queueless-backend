package com.queueless.controller;

import com.queueless.dto.PublicQueueResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.repository.TokenRepository;
import com.queueless.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/queue")
public class PublicQueueController {

    private final QueueService queueService;
    private final TokenRepository tokenRepository;

    public PublicQueueController(
            QueueService queueService,
            TokenRepository tokenRepository
    ) {
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * ✅ LIVE QUEUE STATUS (CORRECT + STABLE)
     */
    @GetMapping("/{queueId}")
    public PublicQueueResponse getLiveQueueStatus(
            @PathVariable UUID queueId,
            @RequestParam(required = false) Integer yourToken
    ) {

        Queue queue = queueService.getQueueById(queueId);

        // 🔔 ALWAYS FETCH LATEST CALLED TOKEN
        int servingToken = tokenRepository
                .findFirstByQueueAndStatusOrderByTokenNumberDesc(
                        queue,
                        TokenStatus.CALLED
                )
                .map(t -> t.getTokenNumber())
                .orElse(0);

        int peopleAhead = 0;
        int estimatedWait = 0;

        if (yourToken != null && yourToken > servingToken) {
            peopleAhead = yourToken - servingToken - 1;
            estimatedWait =
                    peopleAhead * queue.getAvgServiceTimeMinutes();
        }

        return PublicQueueResponse.builder()
                .shopName(queue.getShop().getName())
                .currentToken(servingToken)          // 🔔 NOW SERVING
                .yourToken(yourToken)
                .peopleAhead(Math.max(peopleAhead, 0))
                .estimatedWaitMinutes(Math.max(estimatedWait, 0))
                .queueOpen(queue.getStatus() == QueueStatus.OPEN)
                .build();
    }
}
