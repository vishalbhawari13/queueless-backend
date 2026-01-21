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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final QueueRepository queueRepository;
    private final TokenAttemptRepository tokenAttemptRepository;
    private final SubscriptionService subscriptionService;

    public TokenService(
            TokenRepository tokenRepository,
            QueueRepository queueRepository,
            TokenAttemptRepository tokenAttemptRepository,
            SubscriptionService subscriptionService
    ) {
        this.tokenRepository = tokenRepository;
        this.queueRepository = queueRepository;
        this.tokenAttemptRepository = tokenAttemptRepository;
        this.subscriptionService = subscriptionService;
    }

    /* =====================================================
       CREATE TOKEN (THREAD-SAFE & DUPLICATE-PROOF)
       ===================================================== */
    @Transactional
    public Token generateToken(
            UUID queueId,
            String name,
            String phone,
            double customerLat,
            double customerLng
    ) {

        /* ===============================
           1️⃣ LOCK QUEUE ROW
           =============================== */
        Queue queue = queueRepository.findByIdForUpdate(queueId)
                .orElseThrow(() -> new BusinessException("Queue not found"));

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BusinessException("Queue is closed");
        }

        /* ===============================
           2️⃣ SUBSCRIPTION LIMIT
           =============================== */
        subscriptionService.validateTokenLimit(queue.getShop());

        /* ===============================
           3️⃣ LOCATION CHECK
           =============================== */
        double distance = GeoUtils.distanceInMeters(
                customerLat,
                customerLng,
                queue.getShop().getLatitude(),
                queue.getShop().getLongitude()
        );

        if (distance > queue.getShop().getAllowedRadiusMeters()) {
            throw new BusinessException("You are too far from the shop");
        }

        /* ===============================
           4️⃣ ONE TOKEN PER PHONE
           =============================== */
        if (tokenRepository.existsByQueueAndPhone(queue, phone)) {
            throw new BusinessException(
                    "Token already generated for this number"
            );
        }

        /* ===============================
           5️⃣ RATE LIMIT (ANTI-SPAM)
           =============================== */
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        long attempts =
                tokenAttemptRepository.countByPhoneAndCreatedAtAfter(
                        phone,
                        tenMinutesAgo
                );

        if (attempts >= 5) {
            throw new BusinessException(
                    "Too many attempts. Try again later"
            );
        }

        tokenAttemptRepository.save(
                TokenAttempt.builder()
                        .phone(phone)
                        .queue(queue)
                        .build()
        );

        /* ===============================
           6️⃣ SAFE TOKEN NUMBER GENERATION
           🔥 SOURCE OF TRUTH = TOKEN TABLE
           =============================== */
        int lastTokenNumber =
                tokenRepository.findMaxTokenNumber(queue);

        int nextTokenNumber = lastTokenNumber + 1;

        /* ===============================
           7️⃣ SAVE TOKEN
           =============================== */
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
