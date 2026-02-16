package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.User;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/context")
public class UserContextController {

    private final QueueService queueService;

    public UserContextController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/me")
    public Map<String, Object> getContext(Authentication authentication) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("Unauthorized");
        }

        User user = (User) authentication.getPrincipal();
        Shop shop = user.getShop();

        Map<String, Object> response = new HashMap<>();

        /* ================= USER ================= */
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        response.put("user", userMap);

        /* ================= SHOP ================= */
        if (shop != null) {

            Map<String, Object> shopMap = new HashMap<>();
            shopMap.put("shopId", shop.getId());
            shopMap.put("name", shop.getName());
            shopMap.put("phone", shop.getPhone());
            shopMap.put("active", shop.isActive());
            response.put("shop", shopMap);

            /* ================= QUEUE ================= */
            Queue queue = queueService.getActiveQueueIfExists(shop.getId());

            Map<String, Object> queueMap = new HashMap<>();

            if (queue != null) {
                queueMap.put("exists", true);
                queueMap.put("queueId", queue.getId());
                queueMap.put("status", queue.getStatus());
                queueMap.put("currentToken", queue.getCurrentToken());
                queueMap.put("avgServiceTimeMinutes", queue.getAvgServiceTimeMinutes());
            } else {
                queueMap.put("exists", false);
                queueMap.put("message", "No active queue for today");
            }

            response.put("queue", queueMap);
        }

        return response;
    }
}
