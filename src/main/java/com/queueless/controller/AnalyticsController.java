package com.queueless.controller;

import com.queueless.dto.DailyAnalyticsResponse;
import com.queueless.entity.Queue;
import com.queueless.entity.User;
import com.queueless.entity.enums.TokenStatus;
import com.queueless.repository.TokenRepository;
import com.queueless.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public AnalyticsController(QueueService queueService,
                               TokenRepository tokenRepository,
                               UserRepository userRepository,
                               SubscriptionService subscriptionService) {
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    /** DAILY ANALYTICS FOR ADMIN */
    @GetMapping("/today")
    public DailyAnalyticsResponse today() {

        User admin = getAuthenticatedAdmin();

        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());

        // 💳 PLAN ENFORCEMENT (ANALYTICS)
        subscriptionService.validateAnalyticsAccess(queue.getShop());

        long completed = tokenRepository.countByQueueAndStatus(queue, TokenStatus.COMPLETED);

        int revenue = tokenRepository.sumBillAmountByQueueAndStatus(queue, TokenStatus.COMPLETED);

        int avgBill = completed == 0 ? 0 : (int) (revenue / completed);

        return DailyAnalyticsResponse.builder()
                .totalTokensCompleted((int) completed)
                .totalRevenue(revenue)
                .averageBill(avgBill)
                .build();
    }

    /** SAFE method to get logged-in admin */
    private User getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied: Admin only");
        }

        if (user.getShop() == null) {
            throw new RuntimeException("Admin has no associated shop");
        }

        return user;
    }
}
