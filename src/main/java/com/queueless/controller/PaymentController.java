package com.queueless.controller;

import com.queueless.entity.AdminUser;
import com.queueless.entity.enums.SubscriptionPlan;
import com.queueless.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder(
            @RequestParam SubscriptionPlan plan
    ) throws Exception {

        AdminUser admin = getAuthenticatedAdmin();

        return paymentService.createOrder(
                admin.getShop(),
                plan
        );
    }

    private AdminUser getAuthenticatedAdmin() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof AdminUser)) {
            throw new RuntimeException("Unauthorized");
        }

        return (AdminUser) auth.getPrincipal();
    }
}
