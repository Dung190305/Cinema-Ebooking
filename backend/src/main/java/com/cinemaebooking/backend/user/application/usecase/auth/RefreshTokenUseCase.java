package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.common.exception.domain.UserExceptions;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RefreshTokenRequest;
import com.cinemaebooking.backend.user.application.dto.Response.LoginResponse;
import com.cinemaebooking.backend.user.application.port.JwtProvider;
import com.cinemaebooking.backend.user.application.port.RefreshTokenBlacklistService;
import com.cinemaebooking.backend.user.application.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenBlacklistService blacklistService;

    public LoginResponse execute(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null) {
            throw UserExceptions.invalidCredentials();
        }

        String refreshToken = request.getRefreshToken();

        // 1. Check if token has been revoked
        if (blacklistService.isRevoked(refreshToken)) {
            throw UserExceptions.invalidCredentials();
        }

        // 2. Validate token signature and expiration, then extract userId
        var userId = jwtProvider.extractUserId(refreshToken);

        // 3. Load user to get role
        var user = userRepository.findById(userId)
                .orElseThrow(UserExceptions::unauthorized);

        // 4. Revoke the old refresh token (rotation)
        var expiresAt = jwtProvider.extractExpiration(refreshToken);
        blacklistService.revoke(refreshToken, expiresAt);

        // 5. Generate new access token (short-lived)
        String newAccessToken = jwtProvider.generateToken(user.getId(), user.getRole().name());

        // 6. Generate new refresh token
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId().getValue());

        // 7. Return both tokens + role
        return new LoginResponse(newAccessToken, newRefreshToken, user.getRole().name());
    }
}