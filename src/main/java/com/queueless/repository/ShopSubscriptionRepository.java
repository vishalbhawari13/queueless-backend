package com.queueless.repository;

import com.queueless.entity.Shop;
import com.queueless.entity.ShopSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShopSubscriptionRepository
        extends JpaRepository<ShopSubscription, UUID> {

    Optional<ShopSubscription> findByShopIdAndActiveTrue(UUID shopId);

    Optional<ShopSubscription> findByShopAndActiveTrue(Shop shop);
}
