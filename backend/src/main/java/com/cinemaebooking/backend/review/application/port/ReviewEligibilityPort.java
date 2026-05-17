package com.cinemaebooking.backend.review.application.port;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;

/**
 * Port để kiểm tra user có đủ điều kiện review hay không.
 * Check: booking đã thanh toán + tất cả vé đã được check-in.
 */
public interface ReviewEligibilityPort {

    /**
     * Kiểm tra user có quyền review booking này không.
     * Điều kiện: booking thuộc user + booking đã thanh toán + tất cả vé đã check-in.
     */
    boolean isEligibleToReview(Long userId, Long bookingId);
}
