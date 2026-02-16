package com.queueless.controller;

import com.queueless.dto.ShopRegisterRequest;
import com.queueless.entity.Shop;
import com.queueless.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/register")
    public Shop registerShop(
            @RequestBody @Valid ShopRegisterRequest request
    ) {
        return shopService.registerShop(request);
    }
}
