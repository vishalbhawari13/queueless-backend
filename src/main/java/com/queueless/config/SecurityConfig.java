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
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // 🔓 PUBLIC – CUSTOMER / QR FLOW
                        .requestMatchers(
                                "/q/**",
                                "/queue.html",
                                "/api/public/**",
                                "/api/token/create",
                                "/api/auth/**"
                        ).permitAll()

                        // 🔐 LOGGED-IN USER (shop registration)
                        .requestMatchers("/api/shop/register")
                        .authenticated()

                        // 🔐 ADMIN ONLY
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMIN")

                        // 🔓 Razorpay webhook
                        .requestMatchers("/api/webhook/**")
                        .permitAll()

                        // ❌ EVERYTHING ELSE BLOCKED
                        .anyRequest().denyAll()
                )

                // JWT filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
