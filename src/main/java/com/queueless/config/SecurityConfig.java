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
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        /* ✅ Allow preflight */
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /* ✅ Public pages */
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/payment.html",
                                "/queue.html",
                                "/login.html",
                                "/pricing.html",
                                "/billing.html",
                                "/dashboard.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        /* ✅ Public APIs */
                        .requestMatchers(
                                "/api/public/**",
                                "/api/auth/**",
                                "/api/token/create",
                                "/api/webhook/**"
                        ).permitAll()

                        /* 🔐 Authenticated user */
                        .requestMatchers(
                                "/api/shop/register",
                                "/api/context/me"
                        ).authenticated()

                        /* 🔐 Admin */
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/queue/**"
                        ).hasAuthority("ROLE_ADMIN")

                        /* 🔥 Everything else requires login */
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
