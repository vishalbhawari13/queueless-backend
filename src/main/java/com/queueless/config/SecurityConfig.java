package com.queueless.config;

import com.queueless.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ❌ CSRF not needed for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // 🚫 No HTTP session (JWT based auth)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        /* =====================================================
                           🔓 PUBLIC – CUSTOMER / QR / LIVE QUEUE FLOW
                           ===================================================== */
                        .requestMatchers(
                                "/q/**",                  // QR entry page
                                "/queue.html",            // static HTML
                                "/api/public/**",         // live queue APIs
                                "/api/token/create",      // customer token creation
                                "/api/auth/**"            // user register/login
                        ).permitAll()

                        /* =====================================================
                           🔐 LOGGED-IN USER (SHOP REGISTRATION)
                           ===================================================== */
                        .requestMatchers("/api/shop/register")
                        .authenticated()

                        /* =====================================================
                           🔐 ADMIN ONLY
                           ===================================================== */
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                        /* =====================================================
                           🔓 WEBHOOKS (Razorpay)
                           ===================================================== */
                        .requestMatchers("/api/webhook/**")
                        .permitAll()

                        /* =====================================================
                           ❌ EVERYTHING ELSE BLOCKED
                           ===================================================== */
                        .anyRequest().denyAll()
                )

                // 🔐 JWT authentication filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
