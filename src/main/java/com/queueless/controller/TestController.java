package com.queueless.controller;

import com.queueless.entity.Shop;
import com.queueless.repository.ShopRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private final ShopRepository shopRepository;

    public TestController(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @PostMapping("/shop")
    public Shop createShop() {
        Shop shop = Shop.builder()
                .name("Demo Shop")
                .phone("9999999999")
                .build();

        return shopRepository.save(shop);
    }
}
