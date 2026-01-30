package com.queueless.repository;

import com.queueless.entity.Payment;
import com.queueless.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    List<Payment> findByShopOrderByCreatedAtDesc(Shop shop);
}
