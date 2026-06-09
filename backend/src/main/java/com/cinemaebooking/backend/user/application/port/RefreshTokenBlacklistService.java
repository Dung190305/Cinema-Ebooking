package com.cinemaebooking.backend.user.application.port;

/**
 * Port for revoking and checking revoked refresh tokens.
 */
public interface RefreshTokenBlacklistService {

    /**
     * Revoke a refresh token by adding its hash to the blacklist.
     *
     * @param rawRefreshToken the raw (unhashed) refresh token string
     * @param expiresAt       the expiration timestamp of the token
     */
    void revoke(String rawRefreshToken, java.time.Instant expiresAt);

    /**
     * Check whether a refresh token has been revoked.
     *
     * @param rawRefreshToken the raw (unhashed) refresh token string
     * @return true if revoked, false otherwise
     */
    boolean isRevoked(String rawRefreshToken);
}
