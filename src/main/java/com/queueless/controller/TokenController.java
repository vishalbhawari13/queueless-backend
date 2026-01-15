package com.queueless.controller;

import com.queueless.dto.TokenRequest;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.service.QueueService;
import com.queueless.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;
    private final QueueService queueService;

    public TokenController(TokenService tokenService,
                           QueueService queueService) {
        this.tokenService = tokenService;
        this.queueService = queueService;
    }

    @PostMapping("/create")
    public Token createToken(@Valid @RequestBody TokenRequest request) {

        Queue queue = queueService.getQueueById(request.getQueueId());

        return tokenService.generateToken(
                queue,
                request.getCustomerName(),
                request.getPhone(),
                request.getLatitude(),
                request.getLongitude()
        );
    }



}
