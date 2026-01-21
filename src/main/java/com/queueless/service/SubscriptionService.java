package com.queueless.service;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.ShopSubscription;
import com.queueless.entity.enums.SubscriptionPlan;
import com.queueless.exception.BusinessException;
import com.queueless.repository.ShopSubscriptionRepository;
import com.queueless.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final ShopSubscriptionRepository subscriptionRepository;
    private final TokenRepository tokenRepository;
    private final QueueService queueService;

    public SubscriptionService(
            ShopSubscriptionRepository subscriptionRepository,
            TokenRepository tokenRepository,
            QueueService queueService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.tokenRepository = tokenRepository;
        this.queueService = queueService;
    }

    /* ===============================
       🔑 ACTIVE SUBSCRIPTION
       =============================== */
    public ShopSubscription getActiveSubscription(Shop shop) {

        return subscriptionRepository
                .findByShopIdAndActiveTrue(shop.getId())
                .orElseGet(() ->
                        subscriptionRepository.save(
                                ShopSubscription.builder()
                                        .shop(shop)
                                        .plan(SubscriptionPlan.FREE)
                                        .startDate(LocalDate.now())
                                        .endDate(LocalDate.now().plusYears(10))
                                        .active(true)
                                        .build()
                        )
                );
    }

    /* ===============================
       🎟️ DAILY TOKEN LIMIT
       =============================== */
    public int dailyLimit(Shop shop) {
        return getActiveSubscription(shop)
                .getPlan()
                .getDailyTokenLimit();
    }

    public int tokensUsedToday(Shop shop) {

        // ✅ ALWAYS use today's queue
        Queue queue =
                queueService.getActiveQueueByShopId(shop.getId());

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        return (int) tokenRepository
                .countByQueueAndCreatedAtAfter(queue, startOfDay);
    }

    public void validateTokenLimit(Shop shop) {

        int used = tokensUsedToday(shop);
        int limit = dailyLimit(shop);

        if (used >= limit) {
            throw new BusinessException(
                    "Daily token limit reached (" + limit + "). Upgrade plan."
            );
        }
    }

    /* ===============================
       📊 ANALYTICS ACCESS
       =============================== */
    public void validateAnalyticsAccess(Shop shop) {

        if (!getActiveSubscription(shop)
                .getPlan()
                .isAnalyticsEnabled()) {

            throw new BusinessException(
                    "Upgrade plan to access analytics"
            );
        }
    }

    public void validateAdvancedAnalyticsAccess(Shop shop) {

        if (!getActiveSubscription(shop)
                .getPlan()
                .isAdvancedAnalyticsEnabled()) {

            throw new BusinessException(
                    "Upgrade to PRO plan for advanced analytics"
            );
        }
    }
}
