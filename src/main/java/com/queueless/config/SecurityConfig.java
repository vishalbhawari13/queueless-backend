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
                /* ===============================
                   ❌ CSRF (Not needed for JWT)
                   =============================== */
                .csrf(csrf -> csrf.disable())

                /* ===============================
                   🚫 Stateless session (JWT)
                   =============================== */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /* ===============================
                   🔐 Authorization rules
                   =============================== */
                .authorizeHttpRequests(auth -> auth

                        /* =========================================
                           🔓 STATIC FILES (IMPORTANT FIX)
                           ========================================= */
                        .requestMatchers(
                                "/payment.html",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        /* =========================================
                           🔓 PUBLIC – CUSTOMER / AUTH / QR
                           ========================================= */
                        .requestMatchers(
                                "/q/**",
                                "/queue.html",
                                "/billing.html",
                                "/dashboard.html",
                                "/api/public/**",
                                "/api/token/create",
                                "/api/auth/**"
                        ).permitAll()

                        /* =========================================
                           🔐 LOGGED-IN USER
                           ========================================= */
                        .requestMatchers("/api/shop/register","/api/context/me")
                        .authenticated()

                        /* =========================================
                           🔐 ADMIN ONLY
                           ========================================= */
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/queue/**"
                        ).hasAuthority("ROLE_ADMIN")

                        /* =========================================
                           🔓 WEBHOOKS
                           ========================================= */
                        .requestMatchers("/api/webhook/**")
                        .permitAll()

                        /* =========================================
                           ❌ BLOCK EVERYTHING ELSE
                           ========================================= */
                        .anyRequest().denyAll()
                )

                /* ===============================
                   🔑 JWT FILTER
                   =============================== */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
