package com.queueless.service;

import com.queueless.entity.Shop;
import com.queueless.entity.ShopSubscription;
import com.queueless.entity.enums.SubscriptionPlan;
import com.queueless.exception.BusinessException;
import com.queueless.repository.ShopSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SubscriptionService {

    private final ShopSubscriptionRepository subscriptionRepository;

    public SubscriptionService(ShopSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** Get active subscription or assign FREE */
    public ShopSubscription getActiveSubscription(Shop shop) {

        return subscriptionRepository
                .findByShopIdAndActiveTrue(shop.getId())
                .orElseGet(() -> subscriptionRepository.save(
                        ShopSubscription.builder()
                                .shop(shop)
                                .plan(SubscriptionPlan.FREE)
                                .startDate(LocalDate.now())
                                .active(true)
                                .build()
                ));
    }

    /** Enforce token limit */
    public void validateTokenLimit(
            Shop shop,
            int tokensGeneratedToday
    ) {

        ShopSubscription sub = getActiveSubscription(shop);

        if (tokensGeneratedToday >= sub.getPlan().getDailyTokenLimit()) {
            throw new BusinessException(
                    "Daily token limit reached. Upgrade your plan."
            );
        }
    }

    /** Enforce analytics access */
    public void validateAnalyticsAccess(Shop shop) {

        ShopSubscription sub = getActiveSubscription(shop);

        if (!sub.getPlan().isAnalyticsEnabled()) {
            throw new BusinessException(
                    "Analytics available in paid plans only."
            );
        }
    }
}
