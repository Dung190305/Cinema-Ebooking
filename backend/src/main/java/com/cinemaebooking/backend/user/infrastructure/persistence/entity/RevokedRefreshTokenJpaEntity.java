package com.cinemaebooking.backend.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Stores hashed refresh tokens that have been revoked (logged out or rotated).
 * Unlike most entities, this does NOT use soft delete — revoked tokens are simply
 * deleted after the token's original expiration time has passed.
 */
@Entity
@Table(
        name = "revoked_refresh_tokens",
        indexes = {
                @Index(name = "idx_revoked_token_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_expires_at", columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RevokedRefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of the raw refresh token string.
     * We never store the raw token for security.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * When the original refresh token was supposed to expire.
     * Used for cleanup — expired entries can be purged by a scheduled job.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When this entry was created (at logout time).
     */
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        this.revokedAt = LocalDateTime.now();
    }
}
