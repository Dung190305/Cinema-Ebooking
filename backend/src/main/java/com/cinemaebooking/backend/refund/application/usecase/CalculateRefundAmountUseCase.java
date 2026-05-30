package com.cinemaebooking.backend.refund.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.refund.application.dto.RefundCalculationResponse;
import com.cinemaebooking.backend.refund.domain.policy.RefundPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CalculateRefundAmountUseCase {

    private final BookingRepository bookingRepository;
    private final RefundPolicy refundPolicy;

    @Transactional(readOnly = true)
    public RefundCalculationResponse execute(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw RefundExceptions.notEligible("Chỉ booking đã thanh toán mới đủ điều kiện hoàn tiền.");
        }

        BigDecimal originalAmount = booking.getFinalAmount() != null
                ? booking.getFinalAmount()
                : BigDecimal.ZERO;

        int refundPercentage = refundPolicy.calculatePercentage(
                Instant.now(),
                booking.getShowtimeStartTime()
        );

        BigDecimal refundAmount = refundPolicy.calculateRefundAmount(originalAmount, refundPercentage);

        return new RefundCalculationResponse(
                bookingId,
                originalAmount,
                refundAmount,
                refundPercentage,
                refundPolicy.buildMessage(refundPercentage)
        );
    }
}
