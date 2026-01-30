package com.queueless.controller;

import com.queueless.entity.Payment;
import com.queueless.entity.ShopSubscription;
import com.queueless.entity.User;
import com.queueless.repository.PaymentRepository;
import com.queueless.repository.ShopSubscriptionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentDashboardController {

    private final PaymentRepository paymentRepo;
    private final ShopSubscriptionRepository subRepo;

    public AdminPaymentDashboardController(
            PaymentRepository paymentRepo,
            ShopSubscriptionRepository subRepo
    ) {
        this.paymentRepo = paymentRepo;
        this.subRepo = subRepo;
    }

    /* ===============================
       📜 PAYMENT HISTORY (SHOP ONLY)
       =============================== */
    @GetMapping("/history")
    public List<Payment> paymentHistory(Authentication authentication) {

        User admin = (User) authentication.getPrincipal();

        validateAdmin(admin);

        // ✅ ONLY THIS SHOP'S PAYMENTS
        return paymentRepo.findByShopOrderByCreatedAtDesc(admin.getShop());
    }

    /* ===============================
       💳 CURRENT SUBSCRIPTION (SHOP ONLY)
       =============================== */
    @GetMapping("/current-subscription")
    public ShopSubscription currentSubscription(Authentication authentication) {

        User admin = (User) authentication.getPrincipal();

        validateAdmin(admin);

        // ✅ ONLY THIS SHOP'S ACTIVE SUBSCRIPTION
        return subRepo.findByShopAndActiveTrue(admin.getShop())
                .orElse(null);
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
