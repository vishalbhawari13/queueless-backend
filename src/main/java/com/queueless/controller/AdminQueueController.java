package com.queueless.controller;

import com.queueless.dto.CompleteTokenRequest;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.User;
import com.queueless.repository.TokenRepository;
import com.queueless.repository.UserRepository;
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
    private final UserRepository userRepository;

    public AdminQueueController(AdminQueueService adminQueueService,
                                QueueService queueService,
                                TokenRepository tokenRepository,
                                UserRepository userRepository) {
        this.adminQueueService = adminQueueService;
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    /** CALL NEXT TOKEN */
    @PostMapping("/call-next")
    public Token callNext() {
        User admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());
        return adminQueueService.callNext(queue, admin);
    }

    /** COMPLETE TOKEN */
    @PostMapping("/complete/{tokenId}")
    public Token complete(
            @PathVariable UUID tokenId,
            @RequestBody CompleteTokenRequest request
    ) {
        User admin = getAuthenticatedAdmin();

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
        User admin = getAuthenticatedAdmin();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        return adminQueueService.skipToken(token, admin);
    }

    /** CLOSE QUEUE */
    @PostMapping("/close")
    public Queue close() {
        User admin = getAuthenticatedAdmin();
        Queue queue = queueService.getActiveQueueByShopId(admin.getShop().getId());
        return adminQueueService.closeQueue(queue, admin);
    }

    /** ✅ SAFE way to get logged-in admin */
    private User getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal(); // principal = User object

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied: Admin only");
        }

        return user;
    }
}
