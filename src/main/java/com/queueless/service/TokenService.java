package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final QueueRepository queueRepository;

    public TokenService(TokenRepository tokenRepository,
                        QueueRepository queueRepository) {
        this.tokenRepository = tokenRepository;
        this.queueRepository = queueRepository;
    }

    /**
     * Generate token SAFELY in ONE transaction
     */
    @Transactional
    public Token generateToken(UUID shopId, String name, String phone) {

        // 🔒 LOCKED queue fetch
        Queue queue = queueRepository
                .findByShopIdAndStatus(shopId, QueueStatus.OPEN)
                .orElseThrow(() -> new RuntimeException("No active queue"));

        int nextTokenNumber = queue.getCurrentToken() + 1;

        queue.setCurrentToken(nextTokenNumber);

        Token token = Token.builder()
                .queue(queue)
                .tokenNumber(nextTokenNumber)
                .customerName(name)
                .phone(phone)
                .status(TokenStatus.WAITING)
                .build();

        return tokenRepository.save(token);
    }
}
