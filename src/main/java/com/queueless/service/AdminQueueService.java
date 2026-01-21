package com.queueless.service;

import com.queueless.dto.AdminTokenResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.User;
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

    public AdminQueueService(
            QueueRepository queueRepository,
            TokenRepository tokenRepository
    ) {
        this.queueRepository = queueRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public Token callNext(Queue queue, User admin) {

        validateOwnership(queue, admin);

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        Token next = tokenRepository
                .findByQueueAndStatusOrderByTokenNumberAsc(
                        queue,
                        TokenStatus.WAITING
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("No waiting tokens"));

        next.setStatus(TokenStatus.CALLED);
        tokenRepository.save(next);

        queue.setCurrentToken(next.getTokenNumber());
        queueRepository.save(queue);

        return next;
    }

    @Transactional
    public Token completeToken(
            Token token,
            User admin,
            int billAmount,
            String serviceType
    ) {

        validateOwnership(token.getQueue(), admin);

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new BusinessException("Only CALLED token can be completed");
        }

        token.setStatus(TokenStatus.COMPLETED);
        token.setBillAmount(billAmount);
        token.setServiceType(serviceType);
        tokenRepository.save(token);

        autoCallNext(token.getQueue());
        return token;
    }

    @Transactional
    public Token skipToken(Token token, User admin) {

        validateOwnership(token.getQueue(), admin);

        if (token.getStatus() != TokenStatus.CALLED) {
            throw new BusinessException("Only CALLED token can be skipped");
        }

        token.setStatus(TokenStatus.SKIPPED);
        tokenRepository.save(token);

        autoCallNext(token.getQueue());
        return token;
    }

    @Transactional
    public void closeQueue(Queue queue, User admin) {

        validateOwnership(queue, admin);
        queue.setStatus(QueueStatus.CLOSED);
        queueRepository.save(queue);
    }

    /* ===============================
       DTO MAPPER
       =============================== */
    public AdminTokenResponse toAdminResponse(Token token) {
        return AdminTokenResponse.builder()
                .tokenId(token.getId())
                .tokenNumber(token.getTokenNumber())
                .status(token.getStatus().name())
                .customerName(token.getCustomerName())
                .phone(token.getPhone())
                .queueId(token.getQueue().getId())
                .currentToken(token.getQueue().getCurrentToken())
                .build();
    }

    /* ===============================
       INTERNAL
       =============================== */
    private void autoCallNext(Queue queue) {

        tokenRepository
                .findByQueueAndStatusOrderByTokenNumberAsc(
                        queue,
                        TokenStatus.WAITING
                )
                .stream()
                .findFirst()
                .ifPresent(next -> {
                    next.setStatus(TokenStatus.CALLED);
                    tokenRepository.save(next);

                    queue.setCurrentToken(next.getTokenNumber());
                    queueRepository.save(queue);
                });
    }

    private void validateOwnership(Queue queue, User admin) {
        if (admin.getShop() == null ||
                !queue.getShop().getId().equals(admin.getShop().getId())) {
            throw new BusinessException("Unauthorized action");
        }
    }
}
