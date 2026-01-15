package com.queueless.entity;

import com.queueless.entity.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shop_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopSubscription {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private boolean active;
}
