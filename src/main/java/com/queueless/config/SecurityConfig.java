package com.queueless.config;

import com.queueless.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                /* 🌐 ENABLE CORS */
                .cors(cors -> {})

                /* ❌ CSRF */
                .csrf(csrf -> csrf.disable())

                /* 🚫 STATELESS JWT */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /* 🔐 AUTH RULES */
                .authorizeHttpRequests(auth -> auth

                        /* ✅ ALLOW PREFLIGHT REQUESTS */
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /* 🔓 STATIC */
                        .requestMatchers(
                                "/payment.html",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        /* 🔓 PUBLIC */
                        .requestMatchers(
                                "/q/**",
                                "/queue.html",
                                "/login.html",
                                "/pricing.html",
                                "/billing.html",
                                "/dashboard.html",
                                "/api/public/**",
                                "/api/token/create",
                                "/api/auth/**"
                        ).permitAll()

                        /* 🔐 AUTHENTICATED USER */
                        .requestMatchers(
                                "/api/shop/register",
                                "/api/context/me"
                        ).authenticated()

                        /* 🔐 ADMIN ONLY */
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/queue/**"
                        ).hasAuthority("ROLE_ADMIN")

                        /* 🔓 WEBHOOK */
                        .requestMatchers("/api/webhook/**").permitAll()

                        /* ❌ BLOCK EVERYTHING ELSE */
                        .anyRequest().denyAll()
                )

                /* 🔑 JWT FILTER */
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
