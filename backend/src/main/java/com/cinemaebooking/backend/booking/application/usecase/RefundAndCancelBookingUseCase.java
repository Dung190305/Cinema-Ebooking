package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.RefundPointsUseCase;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundAndCancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final RefundPointsUseCase refundPointsUseCase;

    @Transactional
    public void execute(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            refundPointsUseCase.execute(booking.getUserId(), bookingId);
        }

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            releaseSeatsUseCase.execute(booking.getShowtimeId(), booking.getTickets());
            releaseCouponUseCase.execute(bookingId);

            booking.cancel();
            bookingRepository.save(booking);
        }
    }
}
