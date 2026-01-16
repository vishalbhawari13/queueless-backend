package com.queueless.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class QrPageController {

    @GetMapping("/q/shop/{shopId}")
    public String openQueuePage(@PathVariable UUID shopId) {
        // Internal forward, NOT a redirect
        return "forward:/queue.html";
    }
}
