package com.queueless.controller;

import com.queueless.dto.DailyAnalyticsResponse;
import com.queueless.entity.AdminUser;
import com.queueless.entity.Queue;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.repository.AdminUserRepository;
import com.queueless.repository.TokenRepository;
import com.queueless.service.QueueService;
import com.queueless.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final QueueService queueService;
    private final TokenRepository tokenRepository;
    private final AdminUserRepository adminUserRepository;
    private final SubscriptionService subscriptionService;

    public AnalyticsController(QueueService queueService,
                               TokenRepository tokenRepository,
                               AdminUserRepository adminUserRepository,
                               SubscriptionService subscriptionService) {
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
        this.adminUserRepository = adminUserRepository;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/today")
    public DailyAnalyticsResponse today() {

        AdminUser admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());

        // 💳 PLAN ENFORCEMENT (ANALYTICS)
        subscriptionService.validateAnalyticsAccess(queue.getShop());

        long completed =
                tokenRepository.countByQueueAndStatus(queue, TokenStatus.COMPLETED);

        int revenue =
                tokenRepository.sumBillAmountByQueueAndStatus(queue, TokenStatus.COMPLETED);

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

        return adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
