package com.queueless.service;

import com.queueless.entity.Payment;
import com.queueless.entity.Shop;
import com.queueless.entity.ShopSubscription;
import com.queueless.entity.enums.PaymentStatus;
import com.queueless.entity.enums.SubscriptionPlan;
import com.queueless.exception.BusinessException;
import com.queueless.repository.PaymentRepository;
import com.queueless.repository.ShopSubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class PaymentService {

    /* ===============================
       RAZORPAY CONFIG
       =============================== */
    private final String keyId;
    private final String keySecret;

    private final PaymentRepository paymentRepository;
    private final ShopSubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    public PaymentService(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret,
            PaymentRepository paymentRepository,
            ShopSubscriptionRepository subscriptionRepository,
            EmailService emailService
    ) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.emailService = emailService;
    }

    /* ===============================
       CREATE ORDER
       =============================== */
    public Map<String, Object> createOrder(
            Shop shop,
            SubscriptionPlan plan
    ) throws Exception {

        int amount = plan.getPriceInPaise();

        if (amount <= 0) {
            throw new BusinessException("This plan does not require payment");
        }

        RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

        JSONObject request = new JSONObject();
        request.put("amount", amount);
        request.put("currency", "INR");
        request.put(
                "receipt",
                "shop_" + shop.getId().toString().substring(0, 12)
        );

        Order order = client.orders.create(request);

        paymentRepository.save(
                Payment.builder()
                        .shop(shop)
                        .plan(plan)
                        .amount(amount)
                        .razorpayOrderId(order.get("id"))
                        .status(PaymentStatus.CREATED)
                        .build()
        );

        return Map.of(
                "orderId", order.get("id"),
                "amount", amount,
                "currency", "INR",
                "key", keyId
        );
    }

    /* ===============================
       WEBHOOK HANDLER
       =============================== */
    public void processWebhook(
            String payload,
            String signature,
            String secret
    ) {

        try {
            Utils.verifyWebhookSignature(
                    payload,
                    signature,
                    secret
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid webhook signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        JSONObject entity = json
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String orderId = entity.getString("order_id");
        String paymentId = entity.getString("id");

        if ("payment.captured".equals(event)) {
            handlePaymentSuccess(orderId, paymentId);
        }

        if ("payment.failed".equals(event)) {
            Payment payment = paymentRepository
                    .findByRazorpayOrderId(orderId)
                    .orElseThrow(() ->
                            new BusinessException("Payment not found")
                    );

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    /* ===============================
       PUBLIC SUCCESS METHOD
       (Used by controller / tests)
       =============================== */
    public void handlePaymentSuccess(
            String orderId,
            String paymentId
    ) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(orderId)
                .orElseThrow(() ->
                        new BusinessException("Payment not found")
                );

        handleSuccess(payment, paymentId);
    }

    /* ===============================
       INTERNAL SUCCESS FLOW
       =============================== */
    private void handleSuccess(
            Payment payment,
            String paymentId
    ) {

        if (payment.getStatus() == PaymentStatus.PAID) return;

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        // 🔁 Disable old subscription
        subscriptionRepository
                .findByShopIdAndActiveTrue(
                        payment.getShop().getId()
                )
                .ifPresent(old -> {
                    old.setActive(false);
                    subscriptionRepository.save(old);
                });

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(30);

        subscriptionRepository.save(
                ShopSubscription.builder()
                        .shop(payment.getShop())
                        .plan(payment.getPlan())
                        .startDate(start)
                        .endDate(end)
                        .active(true)
                        .build()
        );

        emailService.sendSubscriptionSuccessEmail(
                payment.getShop(),
                payment.getPlan(),
                start,
                end
        );
    }
}
