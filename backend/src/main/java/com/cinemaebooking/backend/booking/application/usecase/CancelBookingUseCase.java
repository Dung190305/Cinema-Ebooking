package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;

    @Transactional
    public void execute(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        booking.cancel();

        releaseSeatsUseCase.execute(booking.getShowtimeId(), booking.getTickets());
        releaseCouponUseCase.execute(bookingId);

        bookingRepository.save(booking);
    }
}
