package com.queueless.entity.enums;

public enum SubscriptionPlan {

    FREE(20, false),
    BASIC(200, true),
    PRO(Integer.MAX_VALUE, true);

    private final int dailyTokenLimit;
    private final boolean analyticsEnabled;

    SubscriptionPlan(int dailyTokenLimit, boolean analyticsEnabled) {
        this.dailyTokenLimit = dailyTokenLimit;
        this.analyticsEnabled = analyticsEnabled;
    }

    public int getDailyTokenLimit() {
        return dailyTokenLimit;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }
}
