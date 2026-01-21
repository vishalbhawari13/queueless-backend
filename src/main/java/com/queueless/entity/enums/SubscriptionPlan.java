package com.queueless.entity.enums;

public enum SubscriptionPlan {

    FREE(
            0,
            20,
            false,
            false
    ),

    BASIC(
            49900,
            40,
            true,
            false
    ),

    PRO(
            99900,
            70,
            true,
            true
    ),

    PRO_MAX(
            149900,
            200,
            true,
            true
    );

    /* ===============================
       FIELDS
       =============================== */
    private final int priceInPaise;
    private final int dailyTokenLimit;
    private final boolean analyticsEnabled;
    private final boolean advancedAnalyticsEnabled;

    /* ===============================
       CONSTRUCTOR
       =============================== */
    SubscriptionPlan(
            int priceInPaise,
            int dailyTokenLimit,
            boolean analyticsEnabled,
            boolean advancedAnalyticsEnabled
    ) {
        this.priceInPaise = priceInPaise;
        this.dailyTokenLimit = dailyTokenLimit;
        this.analyticsEnabled = analyticsEnabled;
        this.advancedAnalyticsEnabled = advancedAnalyticsEnabled;
    }

    /* ===============================
       GETTERS (USED EVERYWHERE)
       =============================== */
    public int getPriceInPaise() {
        return priceInPaise;
    }

    public int getDailyTokenLimit() {
        return dailyTokenLimit;
    }

    public boolean isAnalyticsEnabled() {
        return analyticsEnabled;
    }

    public boolean isAdvancedAnalyticsEnabled() {
        return advancedAnalyticsEnabled;
    }
}
