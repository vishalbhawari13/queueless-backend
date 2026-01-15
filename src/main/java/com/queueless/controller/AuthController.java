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

    /** 🔐 LOGIN */
    @PostMapping("/login")
    public String login(
            @RequestBody AdminLoginRequest request,
            HttpServletResponse response
    ) {

        AuthService.AuthResponse auth =
                authService.login(request);

        Cookie cookie = new Cookie("refreshToken", auth.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // ✅ true ONLY in production (HTTPS)
        cookie.setPath("/api/admin");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days

        response.addCookie(cookie);

        return auth.accessToken();
    }

    /** 🔁 REFRESH ACCESS TOKEN */
    @PostMapping("/refresh")
    public String refresh(
            @CookieValue("refreshToken") String refreshToken
    ) {
        return authService.refreshAccessToken(refreshToken);
    }
}
