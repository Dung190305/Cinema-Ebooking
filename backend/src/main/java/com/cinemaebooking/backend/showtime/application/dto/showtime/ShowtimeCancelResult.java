package com.cinemaebooking.backend.showtime.application.dto.showtime;

import java.util.List;

/**
 * Result of cancelling a showtime.
 * <p>
 * {@code refundErrors} is non-empty when one or more confirmed bookings
 * could not be auto-refunded. The showtime is still marked CANCELLED in
 * all cases — only the per-booking refund may have partially failed.
 */
public record ShowtimeCancelResult(
        ShowtimeResponse showtime,
        List<RefundError> refundErrors
) {

    public boolean hasRefundErrors() {
        return refundErrors != null && !refundErrors.isEmpty();
    }

    public record RefundError(
            Long bookingId,
            String bookingCode,
            String reason
    ) {}
}