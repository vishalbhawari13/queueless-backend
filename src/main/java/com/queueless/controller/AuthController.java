package com.queueless.controller;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AdminLoginRequest;
import com.queueless.entity.AdminUser;
import com.queueless.entity.RefreshToken;
import com.queueless.repository.AdminUserRepository;
import com.queueless.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AdminUserRepository adminRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AdminUserRepository adminRepo,
                          JwtUtil jwtUtil,
                          BCryptPasswordEncoder encoder,
                          RefreshTokenService refreshTokenService) {
        this.adminRepo = adminRepo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
    }

    /** 🔐 LOGIN */
    @PostMapping("/login")
    public String login(
            @RequestBody AdminLoginRequest request,
            HttpServletResponse response
    ) {

        AdminUser admin = adminRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 1️⃣ ACCESS TOKEN (short-lived)
        String accessToken =
                jwtUtil.generateAccessToken(admin.getUsername());

        // 2️⃣ REFRESH TOKEN (long-lived)
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(admin);

        // 3️⃣ STORE refresh token in HttpOnly cookie
        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // enable on HTTPS
        cookie.setPath("/api/admin/auth");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);

        return accessToken;
    }

    /** 🔁 REFRESH ACCESS TOKEN */
    @PostMapping("/refresh")
    public String refresh(
            @CookieValue("refreshToken") String refreshToken
    ) {

        RefreshToken token =
                refreshTokenService.verifyRefreshToken(refreshToken);

        return jwtUtil.generateAccessToken(
                token.getAdminUser().getUsername()
        );
    }
}
