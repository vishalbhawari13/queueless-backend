package com.queueless.service;

import com.queueless.dto.ShopRegisterRequest;
import com.queueless.entity.Shop;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.ShopRepository;
import com.queueless.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    private final ShopRepository shopRepo;
    private final UserRepository userRepo;
    private final SubscriptionService subscriptionService;

    public ShopService(
            ShopRepository shopRepo,
            UserRepository userRepo,
            SubscriptionService subscriptionService
    ) {
        this.shopRepo = shopRepo;
        this.userRepo = userRepo;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public Shop registerShop(ShopRegisterRequest request) {

        User user = getAuthenticatedUser();

        if (user.getShop() != null) {
            throw new BusinessException("User already owns a shop");
        }

        Shop shop = shopRepo.save(
                Shop.builder()
                        .name(request.getName())
                        .phone(request.getPhone())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .allowedRadiusMeters(request.getAllowedRadiusMeters())
                        .build()
        );

        user.setShop(shop);
        user.setRole("ROLE_ADMIN");
        userRepo.save(user);

        // Assign FREE plan by default
        subscriptionService.getActiveSubscription(shop);

        return shop;
    }

    private User getAuthenticatedUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new BusinessException("Unauthorized");
        }

        return (User) auth.getPrincipal();
    }
}
