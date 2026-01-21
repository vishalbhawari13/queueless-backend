package com.queueless.dto;

import java.util.List;

public record MonthlyAnalyticsResponse(
        List<Integer> dailyCounts
) {}
