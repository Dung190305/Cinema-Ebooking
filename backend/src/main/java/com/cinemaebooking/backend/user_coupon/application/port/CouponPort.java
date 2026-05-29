package com.cinemaebooking.backend.user_coupon.application.port;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponPort {

    CouponSnapshot findValidCoupon(String code, LocalDateTime now);

    record CouponSnapshot(Long id, String code, boolean active, LocalDateTime expiryDate,
                          int pointsToRedeem, int perUserUsage) {}
}