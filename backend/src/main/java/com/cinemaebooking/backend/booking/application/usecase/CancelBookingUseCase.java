package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_combo.application.usecase.ReleaseReservedComboQuantityUseCase;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;
    private final ReleaseReservedComboQuantityUseCase releaseReservedComboQuantityUseCase;
    private final SeatLockService seatLockService; // ← thay ReleaseSeatsUseCase

    @Transactional
    public void execute(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        booking.cancel();

        // PENDING booking: ghế đang LOCKED, không phải BOOKED → dùng SeatLockService
        seatLockService.releaseUserLocks(booking.getUserId(), booking.getShowtimeId());
        releaseReservedComboQuantityUseCase.execute(bookingId);
        releaseCouponUseCase.execute(bookingId);

        bookingRepository.save(booking);
    }
}
