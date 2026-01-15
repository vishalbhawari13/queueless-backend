package com.queueless.controller;

import com.queueless.dto.DailyAnalyticsResponse;
import com.queueless.entity.AdminUser;
import com.queueless.entity.Queue;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.repository.TokenRepository;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final QueueService queueService;
    private final TokenRepository tokenRepository;

    public AnalyticsController(QueueService queueService,
                               TokenRepository tokenRepository) {
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/today")
    public DailyAnalyticsResponse today() {

        AdminUser admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());

        long completed = tokenRepository.countByQueueAndStatus(queue, TokenStatus.COMPLETED);
        int revenue = tokenRepository.sumBillAmountByQueueAndStatus(queue, TokenStatus.COMPLETED);

        int avgBill = completed == 0 ? 0 : (int) (revenue / completed);

        return DailyAnalyticsResponse.builder()
                .totalTokensCompleted((int) completed)
                .totalRevenue(revenue)
                .averageBill(avgBill)
                .build();
    }

    private AdminUser getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // load from DB in real code (same as AdminQueueController)
        throw new UnsupportedOperationException("Reuse admin lookup logic");
    }
}
