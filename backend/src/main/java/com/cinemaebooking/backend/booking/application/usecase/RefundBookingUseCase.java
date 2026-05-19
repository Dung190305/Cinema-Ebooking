package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.RefundPointsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundBookingUseCase {

    private final BookingRepository bookingRepository;
    private final RefundPointsUseCase refundPointsUseCase;

    @Transactional
    public void execute(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw BookingExceptions.invalidStatus(booking.getStatus());
        }

        refundPointsUseCase.execute(booking.getUserId(), bookingId);

        booking.cancel();
        bookingRepository.save(booking);
    }
}
