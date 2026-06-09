package com.cinemaebooking.backend.showtime.application.service;

import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.showtime.application.dto.showtime.ShowtimeCancelResult.RefundError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processes a 100% auto-refund for a single booking when its showtime is cancelled
 * by an admin.  Each booking runs in its own {@code REQUIRES_NEW} transaction so
 * that a failure on one booking does not roll back refunds already created for
 * other bookings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRefundForShowtimeCancellationService {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;

    /**
     * Creates a 100% APPROVED refund for {@code booking} and marks the booking as
     * REFUND_REQUESTED so the admin can complete the payout later.
     *
     * @return {@code null} on success, or a {@link RefundError} describing the failure.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefundError processRefundForBooking(Booking booking) {
        try {
            // Skip if refund already exists (idempotency guard)
            if (refundRepository.existsByBookingId(booking.getId().getValue())) {
                log.warn("Auto-refund skipped: refund already exists for bookingId={}",
                        booking.getId().getValue());
                return null;
            }

            Refund refund = Refund.builder()
                    .bookingId(booking.getId().getValue())
                    .originalAmount(booking.getFinalAmount())
                    .refundAmount(booking.getFinalAmount())   // 100%
                    .refundPercentage(100)
                    .status(RefundStatus.APPROVED)            // pre-approved — admin only needs to complete payout
                    .requestedAt(LocalDateTime.now())
                    .reason("Suất chiếu bị hủy bởi admin — hoàn tiền 100% tự động.")
                    .build();

            refundRepository.create(refund);

            // Mark booking so it shows up as "awaiting payout" in admin panel
            booking.requestRefund();
            bookingRepository.save(booking);

            return null; // success

        } catch (Exception ex) {
            log.error("Auto-refund FAILED for bookingId={}: {}",
                    booking.getId().getValue(), ex.getMessage(), ex);

            return new RefundError(
                    booking.getId().getValue(),
                    booking.getBookingCode(),
                    ex.getMessage()
            );
        }
    }
}