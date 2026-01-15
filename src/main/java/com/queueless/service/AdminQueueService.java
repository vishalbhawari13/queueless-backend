package com.queueless.service;

import com.queueless.entity.AdminUser;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.enums.QueueStatus;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.exception.BusinessException;
import com.queueless.repository.QueueRepository;
import com.queueless.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AdminQueueService {

    private final QueueRepository queueRepository;
    private final TokenRepository tokenRepository;

    public AdminQueueService(QueueRepository queueRepository,
                             TokenRepository tokenRepository) {
        this.queueRepository = queueRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Token callNext(Queue queue, AdminUser admin) {

        validateOwnership(queue, admin);

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        return tokenRepository
                .findByQueueAndStatusOrderByTokenNumberAsc(queue, TokenStatus.WAITING)
                .stream()
                .findFirst()
                .map(token -> {
                    token.setStatus(TokenStatus.CALLED);
                    return tokenRepository.save(token);
                })
                .orElseThrow(() -> new BusinessException("No waiting tokens"));
    }

    @Transactional
    public Token completeToken(Token token, AdminUser admin) {
        validateOwnership(token.getQueue(), admin);

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new BusinessException("Only CALLED token can be completed");
        }

        token.setStatus(TokenStatus.COMPLETED);
        return tokenRepository.save(token);
    }

    @Transactional
    public Token skipToken(Token token, AdminUser admin) {
        validateOwnership(token.getQueue(), admin);

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new BusinessException("Only CALLED token can be skipped");
        }

        token.setStatus(TokenStatus.SKIPPED);
        return tokenRepository.save(token);
    }

    @Transactional
    public Queue closeQueue(Queue queue, AdminUser admin) {
        validateOwnership(queue, admin);
        queue.setStatus(QueueStatus.CLOSED);
        return queueRepository.save(queue);
    }

    private void validateOwnership(Queue queue, AdminUser admin) {
        if (!queue.getShop().getId().equals(admin.getShop().getId())) {
            throw new BusinessException("Unauthorized action");
        }
    }
}
