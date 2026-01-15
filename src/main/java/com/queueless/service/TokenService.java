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
import com.queueless.util.GeoUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final QueueRepository queueRepository;
    private final TokenAttemptRepository tokenAttemptRepository;
    private final SubscriptionService subscriptionService;

    public TokenService(TokenRepository tokenRepository,
                        QueueRepository queueRepository,
                        TokenAttemptRepository tokenAttemptRepository,
                        SubscriptionService subscriptionService) {
        this.tokenRepository = tokenRepository;
        this.queueRepository = queueRepository;
        this.tokenAttemptRepository = tokenAttemptRepository;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public Token generateToken(
            Queue queue,
            String name,
            String phone,
            double customerLat,
            double customerLng
    ) {

        // 1️⃣ Queue must be open
        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        // 2️⃣ 💳 SUBSCRIPTION ENFORCEMENT (DAILY)
        LocalDateTime startOfToday =
                LocalDate.now().atStartOfDay();

        long tokensToday =
                tokenRepository.countByQueueAndCreatedAtAfter(
                        queue,
                        startOfToday
                );

        subscriptionService.validateTokenLimit(
                queue.getShop(),
                (int) tokensToday
        );

        // 3️⃣ 📍 LOCATION CHECK
        double distance = GeoUtils.distanceInMeters(
                customerLat,
                customerLng,
                queue.getShop().getLatitude(),
                queue.getShop().getLongitude()
        );

        if (distance > queue.getShop().getAllowedRadiusMeters()) {
            throw new BusinessException("You are too far from the shop");
        }

        // 4️⃣ One active token per phone per queue
        if (tokenRepository.existsByQueueAndPhone(queue, phone)) {
            throw new BusinessException("Token already generated for this number");
        }

        // 5️⃣ Rate limit: 5 attempts per 10 minutes
        LocalDateTime tenMinutesAgo =
                LocalDateTime.now().minusMinutes(10);

        long attempts =
                tokenAttemptRepository.countByPhoneAndCreatedAtAfter(
                        phone,
                        tenMinutesAgo
                );

        if (attempts >= 5) {
            throw new BusinessException("Too many attempts. Try again later");
        }

        // 6️⃣ Record attempt
        tokenAttemptRepository.save(
                TokenAttempt.builder()
                        .phone(phone)
                        .queue(queue)
                        .build()
        );

        // 7️⃣ Generate token number (safe due to locking + transaction)
        int nextToken = queue.getCurrentToken() + 1;
        queue.setCurrentToken(nextToken);
        queueRepository.save(queue);

        // 8️⃣ Save token
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
