package com.cinemaebooking.backend.user.infrastructure.adapter;

import com.cinemaebooking.backend.user.application.port.JwtProvider;
import com.cinemaebooking.backend.user.domain.valueObject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProviderImpl implements JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration:900000}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(UserId userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId.getValue()))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateResetToken(UserId userId) {

        long RESET_EXPIRATION = 15 * 60 * 1000; // 15 minutes

        return Jwts.builder()
                .setSubject(String.valueOf(userId.getValue()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + RESET_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public UserId extractUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return new UserId(Long.parseLong(subject));
    }

    @Override
    public Instant extractExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.toInstant();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}