package com.queueless.controller;

import com.queueless.dto.TokenRequest;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.service.QueueService;
import com.queueless.service.TokenService;
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
    public Token createToken(@RequestBody TokenRequest request) {

        return tokenService.generateToken(
                request.getShopId(),
                request.getCustomerName(),
                request.getPhone()
        );
    }

}
