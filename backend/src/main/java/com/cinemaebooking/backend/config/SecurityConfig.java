package com.cinemaebooking.backend.config;

import com.cinemaebooking.backend.common.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                )
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight - always allow OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth - public endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Admin scanner page - serve HTML without login (JWT entered inside page)
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/scanner").permitAll()

                        // Admin endpoints - must have ADMIN role
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Public read-only endpoints (GET for all)
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()

                        // All other requests require authentication
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/complete").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/cancel").permitAll()

                        // ⭐ Các GET cần xác thực - đặt TRƯỚC rule permitAll chung
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bookings/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/loyalty/my-account").authenticated()

                        // Các POST/PUT/DELETE liên quan đến booking, user, payment cần auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/**").authenticated()

                        // Tất cả các GET còn lại đều public (phim, suất chiếu, rạp...)
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()

                        // All other requests → require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}