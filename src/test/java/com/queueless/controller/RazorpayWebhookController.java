package com.queueless.controller;

import com.queueless.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.json.JSONObject;

@RestController
@RequestMapping("/api/payment/webhook")
public class RazorpayWebhookController {

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    public RazorpayWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }



    @PostMapping
    public void handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) throws Exception {

        if (!verifySignature(payload, signature)) {
            throw new RuntimeException("Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        if ("payment.captured".equals(event)) {
            JSONObject payment =
                    json.getJSONObject("payload")
                            .getJSONObject("payment")
                            .getJSONObject("entity");

            String orderId = payment.getString("order_id");
            String paymentId = payment.getString("id");

            paymentService.handlePaymentSuccess(orderId, paymentId);
        }
    }


    private boolean verifySignature(
            String payload,
            String actualSignature
    ) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(
                new SecretKeySpec(
                        webhookSecret.getBytes(),
                        "HmacSHA256"
                )
        );

        String expectedSignature =
                Base64.getEncoder()
                        .encodeToString(
                                mac.doFinal(payload.getBytes())
                        );

        return expectedSignature.equals(actualSignature);
    }
}
