package com.queueless.controller;

import com.queueless.dto.PublicQueueResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.service.QueueService;
import com.queueless.repository.TokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/queue")
public class PublicQueueController {

    private final QueueService queueService;
    private final TokenRepository tokenRepository;

    public PublicQueueController(QueueService queueService,
                                 TokenRepository tokenRepository) {
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Live queue status for customers
     */
    @GetMapping("/{queueId}")
    public PublicQueueResponse getLiveQueueStatus(
            @PathVariable UUID queueId,
            @RequestParam(required = false) Integer yourToken
    ) {

        Queue queue = queueService.getQueueById(queueId);

        int currentToken = queue.getCurrentToken();

        int peopleAhead = 0;
        int estimatedWait = 0;

        if (yourToken != null && yourToken > currentToken) {
            peopleAhead = yourToken - currentToken - 1;
            estimatedWait = peopleAhead * queue.getAvgServiceTimeMinutes();
        }

        return PublicQueueResponse.builder()
                .shopName(queue.getShop().getName())
                .currentToken(currentToken)
                .yourToken(yourToken)
                .peopleAhead(Math.max(peopleAhead, 0))
                .estimatedWaitMinutes(Math.max(estimatedWait, 0))
                .queueOpen(queue.getStatus().name().equals("OPEN"))
                .build();
    }
}
