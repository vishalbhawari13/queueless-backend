package com.queueless.controller;

import com.queueless.entity.Payment;
import com.queueless.entity.ShopSubscription;
import com.queueless.repository.PaymentRepository;
import com.queueless.repository.ShopSubscriptionRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @GetMapping("/history")
    public List<Payment> paymentHistory(Principal principal) {
        return paymentRepo.findAll();
    }

    @GetMapping("/current-subscription")
    public ShopSubscription currentSubscription(Principal principal) {
        return subRepo.findAll().stream()
                .filter(ShopSubscription::isActive)
                .findFirst()
                .orElse(null);
    }
}
