package com.queueless.service;

import com.queueless.entity.Payment;
import com.queueless.entity.Shop;
import com.queueless.entity.ShopSubscription;
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

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final ShopSubscriptionRepository subscriptionRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          ShopSubscriptionRepository subscriptionRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Create Razorpay order
     */
    public Map<String, Object> createOrder(
            Shop shop,
            SubscriptionPlan plan
    ) throws Exception {

        int amount = switch (plan) {
            case BASIC -> 49900; // ₹499
            case PRO -> 99900;   // ₹999
            default -> throw new BusinessException("Invalid plan");
        };

        RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

        // ✅ Razorpay receipt must be <= 40 chars
        String receipt =
                "shop_" + shop.getId().toString().substring(0, 12);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        Order order = client.orders.create(orderRequest);

        paymentRepository.save(
                Payment.builder()
                        .shop(shop)
                        .plan(plan)
                        .razorpayOrderId(order.get("id"))
                        .amount(amount)
                        .success(false)
                        .build()
        );

        return Map.of(
                "orderId", order.get("id"),
                "amount", amount,
                "currency", "INR"
        );
    }


    public void processWebhook(
            String payload,
            String signature,
            String secret
    ) {

        try {
            // 🔐 Verify webhook signature
            Utils.verifyWebhookSignature(payload, signature, secret);
        } catch (Exception e) {
            throw new RuntimeException("Invalid webhook signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        // ✅ We only care about successful payments
        if (!event.equals("payment.captured")) {
            return;
        }

        JSONObject paymentEntity =
                json.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String orderId = paymentEntity.getString("order_id");
        String paymentId = paymentEntity.getString("id");

        // 🔁 Idempotent call (safe to call multiple times)
        handlePaymentSuccess(orderId, paymentId);
    }

    /**
     * Called after payment success (webhook / verify API)
     */
    public void handlePaymentSuccess(
            String orderId,
            String paymentId
    ) {

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));

        payment.setRazorpayPaymentId(paymentId);
        payment.setSuccess(true);
        paymentRepository.save(payment);

        // 🔓 Deactivate old subscription if exists
        subscriptionRepository.findByShopIdAndActiveTrue(
                        payment.getShop().getId()
                )
                .ifPresent(old -> {
                    old.setActive(false);
                    subscriptionRepository.save(old);
                });

        // ✅ Activate new subscription
        subscriptionRepository.save(
                ShopSubscription.builder()
                        .shop(payment.getShop())
                        .plan(payment.getPlan())
                        .startDate(LocalDate.now())
                        .active(true)
                        .build()
        );
    }
}
