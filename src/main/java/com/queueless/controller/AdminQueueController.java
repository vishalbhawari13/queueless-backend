package com.queueless.controller;

import com.queueless.dto.CompleteTokenRequest;
import com.queueless.entity.AdminUser;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.repository.AdminUserRepository;
import com.queueless.repository.TokenRepository;
import com.queueless.service.AdminQueueService;
import com.queueless.service.QueueService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/queue")
public class AdminQueueController {

    private final AdminQueueService adminQueueService;
    private final QueueService queueService;
    private final TokenRepository tokenRepository;
    private final AdminUserRepository adminUserRepository;

    public AdminQueueController(AdminQueueService adminQueueService,
                                QueueService queueService,
                                TokenRepository tokenRepository,
                                AdminUserRepository adminUserRepository) {
        this.adminQueueService = adminQueueService;
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
        this.adminUserRepository = adminUserRepository;
    }

    /** CALL NEXT TOKEN */
    @PostMapping("/call-next")
    public Token callNext() {
        AdminUser admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());
        return adminQueueService.callNext(queue, admin);
    }

    /** COMPLETE TOKEN */
    @PostMapping("/complete/{tokenId}")
    public Token complete(
            @PathVariable UUID tokenId,
            @RequestBody CompleteTokenRequest request
    ) {
        AdminUser admin = getAuthenticatedAdmin();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        return adminQueueService.completeToken(
                token,
                admin,
                request.getBillAmount(),
                request.getServiceType()
        );
    }


    /** SKIP TOKEN */
    @PostMapping("/skip/{tokenId}")
    public Token skip(@PathVariable UUID tokenId) {
        AdminUser admin = getAuthenticatedAdmin();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        return adminQueueService.skipToken(token, admin);
    }

    /** CLOSE QUEUE */
    @PostMapping("/close")
    public Queue close() {
        AdminUser admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());
        return adminQueueService.closeQueue(queue, admin);
    }

    /** ✅ SAFE way to get logged-in admin */
    private AdminUser getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // principal = username

        return adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
