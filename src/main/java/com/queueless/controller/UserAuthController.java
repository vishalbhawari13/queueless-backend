package com.queueless.controller;

import com.queueless.dto.LoginRequest;
import com.queueless.dto.RegisterRequest;
import com.queueless.dto.VerifyOtpRequest;
import com.queueless.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    private final UserAuthService authService;

    public UserAuthController(UserAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return "OTP sent to email";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return "Registration successful";
    }

    @PostMapping("/login")
    public String login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        var auth = authService.login(request);

        Cookie cookie = new Cookie("refreshToken", auth.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        return auth.accessToken();
    }
}
