package com.queueless.controller;

import com.queueless.config.JwtUtil;
import com.queueless.dto.AdminLoginRequest;
import com.queueless.entity.AdminUser;
import com.queueless.repository.AdminUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AdminUserRepository adminRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    public AuthController(AdminUserRepository adminRepo,
                          JwtUtil jwtUtil,
                          BCryptPasswordEncoder encoder) {
        this.adminRepo = adminRepo;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public String login(@RequestBody AdminLoginRequest request) {

        AdminUser admin = adminRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(admin.getUsername());
    }

}
