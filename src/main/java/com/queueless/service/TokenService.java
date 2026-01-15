package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.TokenAttempt;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.exception.BusinessException;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.TokenAttemptRepository;
import com.queueless.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final QueueRepository queueRepository;
    private final TokenAttemptRepository tokenAttemptRepository;

    public TokenService(TokenRepository tokenRepository,
                        QueueRepository queueRepository,
                        TokenAttemptRepository tokenAttemptRepository) {
        this.tokenRepository = tokenRepository;
        this.queueRepository = queueRepository;
        this.tokenAttemptRepository = tokenAttemptRepository;
    }

    @Transactional
    public Token generateToken(Queue queue, String name, String phone) {

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        // 1 token per phone per queue
        if (tokenRepository.existsByQueueAndPhone(queue, phone)) {
            throw new BusinessException("Token already generated for this number");
        }

        // Rate limit: 5 attempts / 10 minutes
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long attempts =
                tokenAttemptRepository.countByPhoneAndCreatedAtAfter(phone, tenMinutesAgo);

        if (attempts >= 5) {
            throw new BusinessException("Too many attempts. Try again later");
        }

        // Record attempt
        tokenAttemptRepository.save(
                TokenAttempt.builder()
                        .phone(phone)
                        .queue(queue)
                        .build()
        );

        int nextToken = queue.getCurrentToken() + 1;
        queue.setCurrentToken(nextToken);
        queueRepository.save(queue);

        Token token = Token.builder()
                .queue(queue)
                .tokenNumber(nextToken)
                .customerName(name)
                .phone(phone)
                .status(TokenStatus.WAITING)
                .build();

        return tokenRepository.save(token);
    }
}
