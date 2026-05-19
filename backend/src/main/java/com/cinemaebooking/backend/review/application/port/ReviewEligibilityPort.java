package com.cinemaebooking.backend.review.application.port;
/**
 * Port để kiểm tra user có đủ điều kiện review hay không.
 * Check: booking đã thanh toán + ít nhất vé đã được check-in.
 */
public interface ReviewEligibilityPort {
    boolean isEligibleToReview(Long userId, Long bookingId);
}
