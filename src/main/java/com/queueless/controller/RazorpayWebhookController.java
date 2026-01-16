package com.queueless.controller;

import com.queueless.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webhook/razorpay")
public class RazorpayWebhookController {

    private final PaymentService paymentService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public void handleWebhook(
            HttpServletRequest request,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) throws IOException {

        String payload = request.getReader()
                .lines()
                .collect(Collectors.joining());

        paymentService.processWebhook(payload, signature, webhookSecret);
    }
}
