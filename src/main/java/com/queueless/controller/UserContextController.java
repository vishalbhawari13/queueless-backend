package com.queueless.controller;

import com.queueless.entity.Queue;
import com.queueless.entity.Shop;
import com.queueless.entity.User;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/context")
public class UserContextController {

    private final QueueService queueService;

    public UserContextController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/me")
    public Map<String, Object> getContext(Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        Shop shop = user.getShop();

        Map<String, Object> response = new HashMap<>();

        response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));

        if (shop != null) {
            response.put("shop", Map.of(
                    "shopId", shop.getId(),
                    "name", shop.getName(),
                    "phone", shop.getPhone(),
                    "active", shop.isActive()
            ));

            // ✅ FETCH ACTIVE QUEUE
            Queue queue = queueService.getActiveQueueByShopId(shop.getId());

            if (queue != null) {
                response.put("queue", Map.of(
                        "queueId", queue.getId(),
                        "status", queue.getStatus(),
                        "currentToken", queue.getCurrentToken(),
                        "avgServiceTimeMinutes", queue.getAvgServiceTimeMinutes()
                ));
            } else {
                response.put("queue", Map.of(
                        "exists", false,
                        "message", "No active queue for today"
                ));
            }
        }

        return response;
    }

}
