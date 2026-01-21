package com.queueless.scheduler;

import com.queueless.entity.ShopSubscription;
import com.queueless.entity.enums.SubscriptionPlan;
import com.queueless.repository.ShopSubscriptionRepository;
import com.queueless.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SubscriptionExpiryJob {

    private final ShopSubscriptionRepository repo;
    private final EmailService emailService;

    public SubscriptionExpiryJob(
            ShopSubscriptionRepository repo,
            EmailService emailService
    ) {
        this.repo = repo;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 0 * * *") // daily midnight
    public void expireSubscriptions() {

        List<ShopSubscription> expired =
                repo.findAll().stream()
                        .filter(s -> s.isActive()
                                && s.getEndDate().isBefore(LocalDate.now()))
                        .toList();

        for (ShopSubscription sub : expired) {

            sub.setActive(false);
            repo.save(sub);

            repo.save(
                    ShopSubscription.builder()
                            .shop(sub.getShop())
                            .plan(SubscriptionPlan.FREE)
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusYears(100))
                            .active(true)
                            .build()
            );

            emailService.sendSubscriptionExpiredEmail(
                    sub.getShop(),
                    sub.getPlan()
            );
        }
    }
}
