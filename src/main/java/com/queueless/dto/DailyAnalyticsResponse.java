package com.queueless.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyAnalyticsResponse {

    private int totalTokensCompleted;
    private int totalRevenue;
    private int averageBill;
}
