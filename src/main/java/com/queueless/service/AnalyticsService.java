package com.queueless.service;

import com.queueless.dto.DailyAnalyticsResponse;
import com.queueless.dto.MonthlyAnalyticsResponse;
import com.queueless.entity.Shop;
import com.queueless.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class AnalyticsService {

    private final TokenRepository tokenRepository;

    public AnalyticsService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /* ===============================
       📊 DAILY ANALYTICS
       =============================== */
    public DailyAnalyticsResponse today(Shop shop) {

        int completed =
                tokenRepository.countCompletedToday(shop.getId());

        int revenue =
                tokenRepository.sumRevenueToday(shop.getId());

        int avgBill =
                completed == 0 ? 0 : revenue / completed;

        return DailyAnalyticsResponse.builder()
                .totalTokensCompleted(completed)
                .totalRevenue(revenue)
                .averageBill(avgBill)
                .build();
    }

    /* ===============================
       📈 MONTHLY ANALYTICS
       =============================== */
    public MonthlyAnalyticsResponse monthly(Shop shop) {

        int month = YearMonth.now().getMonthValue();

        List<Integer> dailyCounts =
                tokenRepository.dailyCountsForMonth(
                        shop.getId(),
                        month
                );

        return new MonthlyAnalyticsResponse(dailyCounts);
    }
}
