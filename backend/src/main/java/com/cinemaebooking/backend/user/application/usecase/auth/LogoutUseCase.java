package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.user.application.port.JwtProvider;
import com.cinemaebooking.backend.user.application.port.RefreshTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final JwtProvider jwtProvider;
    private final RefreshTokenBlacklistService blacklistService;

    @Transactional
    public void execute(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        var expiresAt = jwtProvider.extractExpiration(refreshToken);
        blacklistService.revoke(refreshToken, expiresAt);
    }
}
