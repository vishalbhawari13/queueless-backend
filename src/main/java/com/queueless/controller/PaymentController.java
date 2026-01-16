package com.queueless.controller;

import com.queueless.entity.User;
import com.queueless.entity.Shop;
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

    /** CREATE PAYMENT ORDER FOR ADMIN */
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestParam SubscriptionPlan plan) throws Exception {

        User admin = getAuthenticatedAdmin();

        Shop shop = admin.getShop();
        if (shop == null) {
            throw new RuntimeException("Admin has no associated shop");
        }

        return paymentService.createOrder(shop, plan);
    }

    /** SAFE method to get logged-in admin */
    private User getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new RuntimeException("Unauthorized");
        }

        User user = (User) auth.getPrincipal();

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied: Admin only");
        }

        return user;
    }
}
