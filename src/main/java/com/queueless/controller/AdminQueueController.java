package com.queueless.controller;

import com.queueless.dto.AdminTokenResponse;
import com.queueless.dto.CompleteTokenRequest;
import com.queueless.entity.Queue;
import com.queueless.entity.Token;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
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

    public AdminQueueController(
            AdminQueueService adminQueueService,
            QueueService queueService,
            TokenRepository tokenRepository
    ) {
        this.adminQueueService = adminQueueService;
        this.queueService = queueService;
        this.tokenRepository = tokenRepository;
    }

    /* ===============================
       CALL NEXT TOKEN
       =============================== */
    @PostMapping("/call-next")
    public AdminTokenResponse callNext() {

        User admin = getAuthenticatedAdmin();

        Queue queue =
                queueService.getActiveQueueByShopId(
                        admin.getShop().getId()
                );

        Token token =
                adminQueueService.callNext(queue, admin);

        return adminQueueService.toAdminResponse(token);
    }

    /* ===============================
       COMPLETE TOKEN
       =============================== */
    @PostMapping("/complete/{tokenId}")
    public AdminTokenResponse complete(
            @PathVariable UUID tokenId,
            @RequestBody CompleteTokenRequest request
    ) {

        User admin = getAuthenticatedAdmin();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new BusinessException("Token not found"));

        Token completed =
                adminQueueService.completeToken(
                        token,
                        admin,
                        request.getBillAmount(),
                        request.getServiceType()
                );

        return adminQueueService.toAdminResponse(completed);
    }

    /* ===============================
       SKIP TOKEN
       =============================== */
    @PostMapping("/skip/{tokenId}")
    public AdminTokenResponse skip(@PathVariable UUID tokenId) {

        User admin = getAuthenticatedAdmin();

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new BusinessException("Token not found"));

        Token skipped =
                adminQueueService.skipToken(token, admin);

        return adminQueueService.toAdminResponse(skipped);
    }

    /* ===============================
       CLOSE QUEUE
       =============================== */
    @PostMapping("/close")
    public String close() {

        User admin = getAuthenticatedAdmin();

        Queue queue =
                queueService.getActiveQueueByShopId(
                        admin.getShop().getId()
                );

        adminQueueService.closeQueue(queue, admin);
        return "Queue closed successfully";
    }

    /* ===============================
       AUTH
       =============================== */
    private User getAuthenticatedAdmin() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) auth.getPrincipal();

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin only");
        }
        return user;
    }
}
