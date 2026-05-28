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
                        // Auth
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers("/api/v1/users/me/**").authenticated()
                        .requestMatchers("/api/v1/loyalty/my-account/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ✅ Tất cả GET còn lại → public
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/complete").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/cancel").permitAll()

                        // User
                        .requestMatchers("/api/v1/users/me/**").authenticated()

                        // Admin prefix (nếu sau này dùng)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

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