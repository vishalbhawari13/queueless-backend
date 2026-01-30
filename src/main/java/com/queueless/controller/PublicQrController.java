package com.queueless.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/q")
public class PublicQrController {

    /**
     * QR SCAN → OPEN FRONTEND JOIN PAGE
     */
    @GetMapping("/join/{shopId}")
    public void openJoinPage(
            @PathVariable UUID shopId,
            HttpServletResponse response
    ) throws IOException {

        response.sendRedirect(
                "http://localhost:5173/join?shopId=" + shopId
        );
    }
}
