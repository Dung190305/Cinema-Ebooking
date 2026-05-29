package com.cinemaebooking.backend.booking.application.usecase;

import com.cinemaebooking.backend.booking.application.dto.BookingListItemResponse;
import com.cinemaebooking.backend.booking.application.mapper.BookingListItemResponseMapper;
import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserBookingsUseCase {

    private final BookingRepository bookingRepository;
    private final BookingListItemResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<BookingListItemResponse> execute(Long userId, BookingStatus status, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserId(userId, status, pageable);
        bookings.forEach(b -> System.out.println("[GetUserBookings] bookingId=" + b.getId().getValue() + " movieId=" + b.getMovieId() + " movieTitle=" + b.getMovieTitle()));
        return bookings.map(mapper::toListItemResponse);
    }
}
