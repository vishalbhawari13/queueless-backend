package com.queueless.controller;

import com.queueless.dto.TokenRequest;
import com.queueless.entity.Token;
import com.queueless.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/create")
    public Token createToken(
            @Valid @RequestBody TokenRequest request
    ) {
        return tokenService.generateToken(
                request.getQueueId(),
                request.getCustomerName(),
                request.getPhone(),
                request.getLatitude(),
                request.getLongitude()
        );
    }
}
