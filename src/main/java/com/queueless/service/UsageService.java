package com.queueless.service;

import com.queueless.dto.UsageWarningResponse;
import com.queueless.entity.Shop;
import org.springframework.stereotype.Service;

@Service
public class UsageService {

    private final SubscriptionService subscriptionService;

    public UsageService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public UsageWarningResponse checkUsage(Shop shop) {

        int used = subscriptionService.tokensUsedToday(shop);
        int limit = subscriptionService.dailyLimit(shop);

        int percent = (limit == 0) ? 0 : (used * 100) / limit;

        return new UsageWarningResponse(
                used,
                limit,
                percent,
                percent >= 80
        );
    }
}
