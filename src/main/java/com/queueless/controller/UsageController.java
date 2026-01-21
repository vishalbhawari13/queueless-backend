package com.queueless.controller;

import com.queueless.dto.UsageWarningResponse;
import com.queueless.entity.User;
import com.queueless.service.UsageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/warning")
    public UsageWarningResponse warning(Authentication auth) {
        User admin = (User) auth.getPrincipal();
        return usageService.checkUsage(admin.getShop());
    }
}
