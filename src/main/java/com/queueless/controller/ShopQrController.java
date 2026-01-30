package com.queueless.controller;

import com.queueless.entity.Shop;
import com.queueless.entity.User;
import com.queueless.exception.BusinessException;
import com.queueless.repository.ShopRepository;
import com.queueless.util.QrCodeGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/qr")
public class ShopQrController {

    private final ShopRepository shopRepository;

    public ShopQrController(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    /**
     * ADMIN ONLY
     * Generates STATIC QR for shop
     * QR → PUBLIC URL → joins current queue
     */
    @PostMapping("/shop/{shopId}")
    public void generateShopQr(
            @PathVariable UUID shopId,
            HttpServletResponse response
    ) throws Exception {

        User admin = getAdmin();

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException("Shop not found"));

        // 🔒 Ensure admin owns this shop
        if (admin.getShop() == null ||
                !shop.getId().equals(admin.getShop().getId())) {
            throw new BusinessException("Unauthorized");
        }

        /**
         * ✅ QR must point to JOIN endpoint
         * Same QR works every day
         */
        String qrText = "http://localhost:8080/q/join/" + shop.getId();

        BufferedImage qrImage =
                QrCodeGenerator.generateQr(qrText, 300);

        response.setContentType("image/png");
        response.setHeader(
                "Content-Disposition",
                "inline; filename=shop-qr.png"
        );

        ImageIO.write(qrImage, "PNG", response.getOutputStream());
    }

    /**
     * 🔒 ADMIN VALIDATION
     */
    private User getAdmin() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof User)) {
            throw new BusinessException("Unauthorized");
        }

        User user = (User) auth.getPrincipal();

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new BusinessException("Admin only");
        }

        return user;
    }
}
