package com.queueless.controller;

import com.queueless.dto.AdminLoginRequest;
import com.queueless.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /* =====================================================
       🔐 ADMIN LOGIN
       ===================================================== */

    @PostMapping("/login")
    public AuthService.AuthResponse login(
            @RequestBody AdminLoginRequest request,
            HttpServletResponse response
    ) {

        AuthService.AuthResponse auth =
                authService.login(request);

        // 🍪 Set refresh token cookie
        Cookie cookie = new Cookie("refreshToken", auth.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ✅ true in production (HTTPS)
        cookie.setPath("/api/admin");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days

        response.addCookie(cookie);

        return auth; // ✅ return access + refresh token
    }

    /* =====================================================
       🔁 REFRESH ACCESS TOKEN (ROTATION)
       ===================================================== */

    @PostMapping("/refresh")
    public AuthService.AuthResponse refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {

        AuthService.AuthResponse auth =
                authService.refreshAccessToken(refreshToken);

        // 🔁 Update refresh token cookie (ROTATED)
        Cookie cookie = new Cookie("refreshToken", auth.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ✅ true in production
        cookie.setPath("/api/admin");
        cookie.setMaxAge(30 * 24 * 60 * 60);

        response.addCookie(cookie);

        return auth;
    }
}
