package com.cinemaebooking.backend.user.infrastructure.service;

import com.cinemaebooking.backend.user.application.port.RefreshTokenBlacklistService;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.RevokedRefreshTokenJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.RevokedRefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * DB-backed refresh token blacklist.
 * Stores SHA-256 hash of the token (never the raw token) and its expiration time.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenBlacklistServiceImpl implements RefreshTokenBlacklistService {

    private final RevokedRefreshTokenJpaRepository repository;

    @Override
    @Transactional
    public void revoke(String rawRefreshToken, Instant expiresAt) {
        String hash = sha256(rawRefreshToken);
        LocalDateTime expiresAtLocal = LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());

        repository.save(RevokedRefreshTokenJpaEntity.builder()
                .tokenHash(hash)
                .expiresAt(expiresAtLocal)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(String rawRefreshToken) {
        return repository.existsByTokenHash(sha256(rawRefreshToken));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
