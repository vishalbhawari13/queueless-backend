package com.queueless.controller;

import com.queueless.dto.DailyAnalyticsResponse;
import com.queueless.dto.MonthlyAnalyticsResponse;
import com.queueless.entity.User;
import com.queueless.service.AnalyticsService;
import com.queueless.service.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SubscriptionService subscriptionService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            SubscriptionService subscriptionService
    ) {
        this.analyticsService = analyticsService;
        this.subscriptionService = subscriptionService;
    }

    /* ===============================
       📊 DAILY ANALYTICS
       =============================== */
    @GetMapping("/today")
    public DailyAnalyticsResponse today(Authentication authentication) {

        User admin = (User) authentication.getPrincipal();

        validateAdmin(admin);

        // 🔐 PLAN CHECK (CANNOT BE BYPASSED)
        //subscriptionService.validateAnalyticsAccess(admin.getShop());

        return analyticsService.today(admin.getShop());
    }

    /* ===============================
       📈 MONTHLY ANALYTICS
       =============================== */
    @GetMapping("/monthly")
    public MonthlyAnalyticsResponse monthly(Authentication authentication) {

        User admin = (User) authentication.getPrincipal();

        validateAdmin(admin);

        // 🔐 ADVANCED ANALYTICS ONLY (PRO / PRO MAX)
        subscriptionService.validateAdvancedAnalyticsAccess(admin.getShop());

        return analyticsService.monthly(admin.getShop());
    }

    /* ===============================
       🔒 COMMON VALIDATION
       =============================== */
    private void validateAdmin(User user) {
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin access only");
        }
        if (user.getShop() == null) {
            throw new RuntimeException("Admin has no shop");
        }
    }
}
